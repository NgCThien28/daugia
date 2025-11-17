package com.example.daugia.service;

import com.example.daugia.core.enums.TrangThaiPhienDauGia;
import com.example.daugia.core.enums.TrangThaiPhieuThanhToanTienCoc;
import com.example.daugia.entity.Phiendaugia;
import com.example.daugia.entity.Phieuthanhtoantiencoc;
import com.example.daugia.repository.PhiendaugiaRepository;
import com.example.daugia.repository.PhieuthanhtoantiencocRepository;
import jakarta.annotation.PostConstruct;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuctionSchedulerService {

    @Autowired
    private ThreadPoolTaskScheduler scheduler;
    @Autowired
    private PhiendaugiaRepository phiendaugiaRepository;
    @Autowired
    private PhieuthanhtoantiencocRepository phieuthanhtoantiencocRepository;
    @Autowired
    private EmailService emailService;

    // _start, _end, _notify
    private final Map<String, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();
    private final Set<String> preStartNotifiedSessions = ConcurrentHashMap.newKeySet();

    private static final long ONE_DAY_MS = 24L * 60 * 60 * 1000;

    @PostConstruct
    public void init() {
        log.info("AuctionSchedulerService initializing...");
        try {
            syncAndScheduleAllAuctions();
        } catch (Exception e) {
            log.error("Failed initial syncAndScheduleAllAuctions", e);
        }
        // Periodic resync chỉ để bắt phiên mới / cập nhật — không dựa vào nó cho notify 24h
        scheduler.scheduleAtFixedRate(this::safeSyncAndScheduleAllAuctions, 30 * 60_000L);
        log.info("AuctionSchedulerService initialized (30m resync).");
    }

    private void safeSyncAndScheduleAllAuctions() {
        try {
            syncAndScheduleAllAuctions();
        } catch (Exception e) {
            log.error("Error during periodic sync", e);
        }
    }

    private void syncAndScheduleAllAuctions() throws MessagingException, IOException {
        List<Phiendaugia> all = phiendaugiaRepository.findAll();
        long now = System.currentTimeMillis();
        log.info("Synchronizing {} auctions. Now={}", all.size(), now);

        for (Phiendaugia phien : all) {
            if (phien == null || phien.getMaphiendg() == null
                    || phien.getThoigianbd() == null || phien.getThoigiankt() == null) {
                continue;
            }

            long startTime = phien.getThoigianbd().getTime();
            long endTime = phien.getThoigiankt().getTime();

            // Schedule notify BEFORE anything else
            schedulePreStartNotifyOnce(phien, now, startTime);

            // 1) Quá endTime
            if (now >= endTime) {
                if (phien.getTrangthai() != TrangThaiPhienDauGia.WAITING_FOR_PAYMENT) {
                    endAuctionSafe(phien);
                } else {
                    cancelScheduledTask(phien.getMaphiendg());
                }
                continue;
            }

            // 2) Đang trong phiên
            if (now >= startTime) {
                if (phien.getTrangthai() == TrangThaiPhienDauGia.NOT_STARTED
                        || phien.getTrangthai() == TrangThaiPhienDauGia.APPROVED) {
                    startAuctionSafe(phien);
                }
                scheduleEndOnce(phien);
                continue;
            }

            // 3) Chưa tới giờ bắt đầu
            if (phien.getTrangthai() == TrangThaiPhienDauGia.APPROVED) {
                phien.setTrangthai(TrangThaiPhienDauGia.NOT_STARTED);
                try {
                    phiendaugiaRepository.save(phien);
                } catch (Exception e) {
                    log.warn("Cannot mark NOT_STARTED for {}: {}", phien.getMaphiendg(), e.getMessage());
                }
            }

            scheduleStartOnce(phien);
            scheduleEndOnce(phien);
        }
    }

    private void startAuctionSafe(Phiendaugia phien) {
        String startKey = phien.getMaphiendg() + "_start";
        ScheduledFuture<?> f = scheduledTasks.remove(startKey);
        if (f != null) f.cancel(false);

        if (phien.getTrangthai() == TrangThaiPhienDauGia.IN_PROGRESS
                || phien.getTrangthai() == TrangThaiPhienDauGia.WAITING_FOR_PAYMENT) {
            return;
        }
        startAuction(phien);
    }

    private void endAuctionSafe(Phiendaugia phien) {
        cancelScheduledTask(phien.getMaphiendg());
        if (phien.getTrangthai() == TrangThaiPhienDauGia.WAITING_FOR_PAYMENT) return;
        endAuction(phien);
    }

    private void scheduleStartOnce(Phiendaugia phien) {
        String key = phien.getMaphiendg() + "_start";
        if (scheduledTasks.containsKey(key)) return;

        long delay = phien.getThoigianbd().getTime() - System.currentTimeMillis();
        if (delay <= 0) return;

        ScheduledFuture<?> future = scheduler.schedule(() -> {
            try {
                Phiendaugia fresh = phiendaugiaRepository.findById(phien.getMaphiendg()).orElse(phien);
                startAuctionSafe(fresh);
            } catch (Exception e) {
                log.error("Scheduled start error {}: {}", phien.getMaphiendg(), e.getMessage());
            } finally {
                scheduledTasks.remove(key);
            }
        }, Instant.ofEpochMilli(System.currentTimeMillis() + delay));

        scheduledTasks.put(key, future);
    }

    private void scheduleEndOnce(Phiendaugia phien) {
        String key = phien.getMaphiendg() + "_end";
        if (scheduledTasks.containsKey(key)) return;

        long delay = phien.getThoigiankt().getTime() - System.currentTimeMillis();
        if (delay <= 0) {
            endAuctionSafe(phien);
            return;
        }

        ScheduledFuture<?> future = scheduler.schedule(() -> {
            try {
                Phiendaugia fresh = phiendaugiaRepository.findById(phien.getMaphiendg()).orElse(phien);
                endAuctionSafe(fresh);
            } catch (Exception e) {
                log.error("Scheduled end error {}: {}", phien.getMaphiendg(), e.getMessage());
            } finally {
                scheduledTasks.remove(key);
            }
        }, Instant.ofEpochMilli(System.currentTimeMillis() + delay));

        scheduledTasks.put(key, future);
    }

    private void startAuction(Phiendaugia phien) {
        try {
            if (phien.getTrangthai() == TrangThaiPhienDauGia.IN_PROGRESS) return;
            phien.setTrangthai(TrangThaiPhienDauGia.IN_PROGRESS);
            phiendaugiaRepository.save(phien);
            log.info("▶ Phiên {} → IN_PROGRESS", phien.getMaphiendg());
        } catch (Exception e) {
            log.error("Start auction failed {}: {}", phien.getMaphiendg(), e.getMessage());
        }
    }

    private void endAuction(Phiendaugia phien) {
        try {
            if (phien.getTrangthai() == TrangThaiPhienDauGia.WAITING_FOR_PAYMENT) return;
            phien.setTrangthai(TrangThaiPhienDauGia.WAITING_FOR_PAYMENT);
            phiendaugiaRepository.save(phien);
            scheduledTasks.remove(phien.getMaphiendg() + "_start");
            scheduledTasks.remove(phien.getMaphiendg() + "_end");
            scheduledTasks.remove(phien.getMaphiendg() + "_notify");
            log.info("✅ Phiên {} → WAITING_FOR_PAYMENT", phien.getMaphiendg());
        } catch (Exception e) {
            log.error("End auction failed {}: {}", phien.getMaphiendg(), e.getMessage());
        }
    }

    public void cancelScheduledTask(String maphiendg) {
        if (maphiendg == null) return;
        for (String suffix : new String[] { "_start", "_end", "_notify" }) {
            String key = maphiendg + suffix;
            ScheduledFuture<?> f = scheduledTasks.remove(key);
            if (f != null) {
                try { f.cancel(false); } catch (Exception ignored) {}
            }
        }
    }

    /**
     * NEW: Schedule thông báo 24h trước. Nếu:
     *  - startTime - now > 24h: tạo task và chờ
     *  - startTime - now <= 24h nhưng > 0: gửi ngay lập tức
     *  - startTime <= now: bỏ qua
     *  - Chỉ gửi 1 lần (set đánh dấu)
     */
    private void schedulePreStartNotifyOnce(Phiendaugia phien, long now, long startTime) throws MessagingException, IOException {
        if (preStartNotifiedSessions.contains(phien.getMaphiendg())) return;
        long diff = startTime - now;

        if (diff <= 0) {
            // phiên đã hoặc đang bắt đầu → không gửi
            return;
        }

        // Còn nhiều hơn 24h: schedule một task ở thời điểm (startTime - 24h)
        if (diff > ONE_DAY_MS) {
            long notifyAt = startTime - ONE_DAY_MS;
            long delay = notifyAt - now;
            String key = phien.getMaphiendg() + "_notify";
            if (scheduledTasks.containsKey(key)) return;

            ScheduledFuture<?> future = scheduler.schedule(() -> {
                try {
                    doNotifyPreStart(phien);
                } catch (Exception e) {
                    log.error("Notify (scheduled) error {}: {}", phien.getMaphiendg(), e.getMessage());
                } finally {
                    scheduledTasks.remove(key);
                }
            }, Instant.ofEpochMilli(notifyAt));

            scheduledTasks.put(key, future);
            log.debug("Scheduled pre-start notify for {} at {} (delay={}ms)",
                    phien.getMaphiendg(), notifyAt, delay);
            return;
        }

        // Còn <= 24h: gửi ngay một lần
        doNotifyPreStart(phien);
    }

    private void doNotifyPreStart(Phiendaugia phien) throws MessagingException, IOException {
        if (preStartNotifiedSessions.contains(phien.getMaphiendg())) return;
        // Re-fetch để tránh dữ liệu cũ (optional)
        Phiendaugia fresh = phiendaugiaRepository.findById(phien.getMaphiendg()).orElse(phien);

        if (!(fresh.getTrangthai() == TrangThaiPhienDauGia.NOT_STARTED
                || fresh.getTrangthai() == TrangThaiPhienDauGia.APPROVED)) {
            log.debug("Skip notify {} because status={}", fresh.getMaphiendg(), fresh.getTrangthai());
            return;
        }

        List<Phieuthanhtoantiencoc> paid =
                phieuthanhtoantiencocRepository.findByPhienDauGia_MaphiendgAndTrangthai(
                        fresh.getMaphiendg(), TrangThaiPhieuThanhToanTienCoc.PAID);

        log.info("Sending pre-start (24h) notify for {} -> {} paid deposits",
                fresh.getMaphiendg(), paid.size());

        for (Phieuthanhtoantiencoc p : paid) {
            if (p.getTaiKhoan() != null) {
                emailService.sendAuctionBeginEmail(p.getTaiKhoan(), fresh);
            }
        }
        preStartNotifiedSessions.add(fresh.getMaphiendg());
    }

    public void scheduleNewOrApprovedAuction(String maphiendg) throws MessagingException, IOException {
        if (maphiendg == null) return;
        Phiendaugia phien = phiendaugiaRepository.findById(maphiendg).orElse(null);
        if (phien == null || phien.getThoigianbd() == null || phien.getThoigiankt() == null) {
            log.warn("scheduleNewOrApprovedAuction: {} not found or missing times", maphiendg);
            return;
        }

        long now = System.currentTimeMillis();
        long startTime = phien.getThoigianbd().getTime();
        long endTime = phien.getThoigiankt().getTime();

        schedulePreStartNotifyOnce(phien, now, startTime);

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
                log.warn("Failed status save {}: {}", maphiendg, e.getMessage());
            }
        }
        scheduleStartOnce(phien);
        scheduleEndOnce(phien);
    }
}