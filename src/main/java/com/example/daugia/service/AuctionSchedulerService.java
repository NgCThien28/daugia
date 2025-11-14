package com.example.daugia.service;

import com.example.daugia.core.enums.TrangThaiPhienDauGia;
import com.example.daugia.entity.Phiendaugia;
import com.example.daugia.repository.PhiendaugiaRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

/**
 * AuctionSchedulerService (refactored)
 *
 * - Không tạo task trùng lặp
 * - Hủy task cũ khi cần
 * - Nếu thời điểm start/end đã tới thì thực thi ngay (không schedule)
 * - Task sau khi chạy sẽ tự remove khỏi scheduledTasks
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuctionSchedulerService {

    @Autowired
    private ThreadPoolTaskScheduler scheduler;
    @Autowired
    private PhiendaugiaRepository phiendaugiaRepository;

    // map key = "<maphiendg>_start" hoặc "<maphiendg>_end"
    private final Map<String, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        log.info("AuctionSchedulerService initializing...");
        // đồng bộ lần đầu
        try {
            syncAndScheduleAllAuctions();
        } catch (Exception e) {
            log.error("Failed initial syncAndScheduleAllAuctions", e);
        }

        // đồng bộ định kỳ mỗi 30 phút (30 * 60_000 ms)
        scheduler.scheduleAtFixedRate(this::safeSyncAndScheduleAllAuctions, 30 * 60_000L);
        log.info("AuctionSchedulerService initialized and scheduled periodic sync (30 minutes).");
    }

    // wrapper để catch exception trong scheduled task
    private void safeSyncAndScheduleAllAuctions() {
        try {
            syncAndScheduleAllAuctions();
        } catch (Exception e) {
            log.error("Error during periodic syncAndScheduleAllAuctions", e);
        }
    }

    /**
     * Đồng bộ DB và schedule các mốc cho từng phiên
     *
     * Nguyên tắc:
     * 1) Nếu now >= endTime  => end ngay (nếu cần) và KHÔNG schedule
     * 2) Else if now >= startTime => start ngay (nếu cần) và schedule end nếu cần
     * 3) Else (chưa tới start) => schedule start (nếu cần) và schedule end (nếu cần)
     */
    private void syncAndScheduleAllAuctions() {
        List<Phiendaugia> allAuctions = phiendaugiaRepository.findAll();
        long now = System.currentTimeMillis();
        log.info("Synchronizing {} auctions. Now = {}", allAuctions.size(), now);

        for (Phiendaugia phien : allAuctions) {
            if (phien == null || phien.getMaphiendg() == null) continue;

            long startTime = phien.getThoigianbd().getTime();
            long endTime = phien.getThoigiankt().getTime();

            // 1) Nếu đã quá endTime -> end ngay (nếu trạng thái chưa được chuyển)
            if (now >= endTime) {
                if (phien.getTrangthai() != TrangThaiPhienDauGia.WAITING_FOR_PAYMENT) {
                    log.info("Now >= endTime for {} -> ending immediately", phien.getMaphiendg());
                    endAuctionSafe(phien);
                } else {
                    // ensure any scheduled tasks are removed
                    cancelScheduledTask(phien.getMaphiendg());
                }
                continue;
            }

            // 2) Nếu đã tới hoặc sau startTime (nhưng trước endTime)
            if (now >= startTime) {
                if (phien.getTrangthai() == TrangThaiPhienDauGia.NOT_STARTED
                        || phien.getTrangthai() == TrangThaiPhienDauGia.APPROVED) {
                    log.info("Now >= startTime for {} -> starting immediately", phien.getMaphiendg());
                    startAuctionSafe(phien);
                }
                // schedule end only once
                scheduleEndOnce(phien);
                continue;
            }

            // 3) Chưa đến giờ bắt đầu: nếu là APPROVED -> mark NOT_STARTED, và schedule start & end
            if (phien.getTrangthai() == TrangThaiPhienDauGia.APPROVED) {
                phien.setTrangthai(TrangThaiPhienDauGia.NOT_STARTED);
                try {
                    phiendaugiaRepository.save(phien);
                    log.debug("Marked {} as NOT_STARTED (was APPROVED)", phien.getMaphiendg());
                } catch (Exception e) {
                    log.warn("Failed to save status change for {}: {}", phien.getMaphiendg(), e.getMessage());
                }
            }

            // schedule start and end if necessary (each will check duplicates)
            scheduleStartOnce(phien);
            scheduleEndOnce(phien);
        }
    }

    // =========== Safe wrappers to avoid race & double actions ===========

    private void startAuctionSafe(Phiendaugia phien) {
        // cancel any scheduled start task (we are starting now)
        String startKey = phien.getMaphiendg() + "_start";
        ScheduledFuture<?> s = scheduledTasks.remove(startKey);
        if (s != null) {
            s.cancel(false);
            log.debug("Cancelled pending start task for {}", phien.getMaphiendg());
        }

        // perform start only if not already in-progress or finished
        if (phien.getTrangthai() == TrangThaiPhienDauGia.IN_PROGRESS
                || phien.getTrangthai() == TrangThaiPhienDauGia.WAITING_FOR_PAYMENT) {
            log.debug("Start skipped for {} because status is {}", phien.getMaphiendg(), phien.getTrangthai());
            return;
        }

        startAuction(phien);
    }

    private void endAuctionSafe(Phiendaugia phien) {
        // cancel pending start and end tasks
        cancelScheduledTask(phien.getMaphiendg());

        // perform end only if not already WAITING_FOR_PAYMENT
        if (phien.getTrangthai() == TrangThaiPhienDauGia.WAITING_FOR_PAYMENT) {
            log.debug("End skipped for {} because already WAITING_FOR_PAYMENT", phien.getMaphiendg());
            return;
        }

        endAuction(phien);
    }

    // =========== Scheduling helpers (idempotent) ===========

    private void scheduleStartOnce(Phiendaugia phien) {
        String key = phien.getMaphiendg() + "_start";
        if (scheduledTasks.containsKey(key)) {
            // already scheduled
            return;
        }

        long delay = phien.getThoigianbd().getTime() - System.currentTimeMillis();
        if (delay <= 0) {
            // start time already passed; handled elsewhere
            return;
        }

        ScheduledFuture<?> future = scheduler.schedule(() -> {
            try {
                // re-fetch latest state from DB to avoid stale entity issues
                Phiendaugia fresh = phiendaugiaRepository.findById(phien.getMaphiendg()).orElse(phien);
                startAuctionSafe(fresh);
            } catch (Exception e) {
                log.error("Error executing scheduled start for {}: {}", phien.getMaphiendg(), e.getMessage(), e);
            } finally {
                scheduledTasks.remove(key);
            }
        }, Instant.ofEpochMilli(System.currentTimeMillis() + delay));

        scheduledTasks.put(key, future);
        log.debug("Scheduled start for {} in {} ms", phien.getMaphiendg(), delay);
    }

    private void scheduleEndOnce(Phiendaugia phien) {
        String key = phien.getMaphiendg() + "_end";
        if (scheduledTasks.containsKey(key)) {
            return;
        }

        long delay = phien.getThoigiankt().getTime() - System.currentTimeMillis();
        if (delay <= 0) {
            // end time already passed -> handle immediately
            endAuctionSafe(phien);
            return;
        }

        ScheduledFuture<?> future = scheduler.schedule(() -> {
            try {
                Phiendaugia fresh = phiendaugiaRepository.findById(phien.getMaphiendg()).orElse(phien);
                endAuctionSafe(fresh);
            } catch (Exception e) {
                log.error("Error executing scheduled end for {}: {}", phien.getMaphiendg(), e.getMessage(), e);
            } finally {
                scheduledTasks.remove(key);
            }
        }, Instant.ofEpochMilli(System.currentTimeMillis() + delay));

        scheduledTasks.put(key, future);
        log.debug("Scheduled end for {} in {} ms", phien.getMaphiendg(), delay);
    }

    // =========== Actual state change methods (persist to DB) ===========

    /**
     * Thực sự bắt đầu phiên (ghi DB)
     * Không cancel các task khác ở đây (được xử lý trong startAuctionSafe)
     */
    private void startAuction(Phiendaugia phien) {
        try {
            if (phien.getTrangthai() == TrangThaiPhienDauGia.IN_PROGRESS) {
                log.debug("startAuction: {} already IN_PROGRESS", phien.getMaphiendg());
                return;
            }
            phien.setTrangthai(TrangThaiPhienDauGia.IN_PROGRESS);
            phiendaugiaRepository.save(phien);
            log.info("▶ Phiên {} chuyển sang IN_PROGRESS", phien.getMaphiendg());
        } catch (Exception e) {
            log.error("Failed to start auction {}: {}", phien.getMaphiendg(), e.getMessage(), e);
        }
    }

    /**
     * Thực sự kết thúc phiên (ghi DB)
     */
    private void endAuction(Phiendaugia phien) {
        try {
            if (phien.getTrangthai() == TrangThaiPhienDauGia.WAITING_FOR_PAYMENT) {
                log.debug("endAuction: {} already WAITING_FOR_PAYMENT", phien.getMaphiendg());
                return;
            }
            phien.setTrangthai(TrangThaiPhienDauGia.WAITING_FOR_PAYMENT);
            phiendaugiaRepository.save(phien);
            // ensure any scheduled tasks are removed
            scheduledTasks.remove(phien.getMaphiendg() + "_start");
            scheduledTasks.remove(phien.getMaphiendg() + "_end");
            log.info("✅ Phiên {} kết thúc → WAITING_FOR_PAYMENT", phien.getMaphiendg());
        } catch (Exception e) {
            log.error("Failed to end auction {}: {}", phien.getMaphiendg(), e.getMessage(), e);
        }
    }

    // =========== Public utilities ===========

    /**
     * Hủy các task scheduled cho phiên (nếu có)
     */
    public void cancelScheduledTask(String maphiendg) {
        if (maphiendg == null) return;

        String startKey = maphiendg + "_start";
        String endKey = maphiendg + "_end";

        ScheduledFuture<?> startFuture = scheduledTasks.remove(startKey);
        if (startFuture != null) {
            try {
                startFuture.cancel(false);
                log.debug("Cancelled scheduled start for {}", maphiendg);
            } catch (Exception e) {
                log.warn("Failed to cancel start task for {}: {}", maphiendg, e.getMessage());
            }
        }

        ScheduledFuture<?> endFuture = scheduledTasks.remove(endKey);
        if (endFuture != null) {
            try {
                endFuture.cancel(false);
                log.debug("Cancelled scheduled end for {}", maphiendg);
            } catch (Exception e) {
                log.warn("Failed to cancel end task for {}: {}", maphiendg, e.getMessage());
            }
        }
    }

    /**
     * Khi admin thêm phiên mới hoặc duyệt -> gọi phương thức này để schedule cho phiên cụ thể
     */
    public void scheduleNewOrApprovedAuction(String maphiendg) {
        if (maphiendg == null) return;
        Phiendaugia phien = phiendaugiaRepository.findById(maphiendg).orElse(null);
        if (phien == null) {
            log.warn("scheduleNewOrApprovedAuction: auction {} not found", maphiendg);
            return;
        }

        // resync single auction
        long now = System.currentTimeMillis();
        long startTime = phien.getThoigianbd().getTime();
        long endTime = phien.getThoigiankt().getTime();

        if (now >= endTime) {
            endAuctionSafe(phien);
            return;
        }

        if (now >= startTime) {
            startAuctionSafe(phien);
            scheduleEndOnce(phien);
            return;
        }

        if (phien.getTrangthai() == TrangThaiPhienDauGia.APPROVED) {
            phien.setTrangthai(TrangThaiPhienDauGia.NOT_STARTED);
            try {
                phiendaugiaRepository.save(phien);
            } catch (Exception e) {
                log.warn("Failed to save status for {}: {}", maphiendg, e.getMessage());
            }
        }

        scheduleStartOnce(phien);
        scheduleEndOnce(phien);
    }
}
