package com.example.daugia.service;

import com.example.daugia.core.enums.TrangThaiPhienDauGia;
import com.example.daugia.core.enums.TrangThaiPhieuThanhToan;
import com.example.daugia.core.enums.TrangThaiPhieuThanhToanTienCoc;
import com.example.daugia.entity.Phiendaugia;
import com.example.daugia.entity.Phientragia;
import com.example.daugia.entity.Phieuthanhtoan;
import com.example.daugia.entity.Phieuthanhtoantiencoc;
import com.example.daugia.repository.PhiendaugiaRepository;
import com.example.daugia.repository.PhieuthanhtoantiencocRepository;
import com.example.daugia.repository.PhientragiaRepository;
import jakarta.annotation.PostConstruct;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Comparator;
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
    private PhientragiaRepository phientragiaRepository;
    @Autowired
    private PhieuthanhtoanService phieuthanhtoanService; // Inject service mới
    @Autowired
    private EmailService emailService;

    // _start, _end, _notify, _payment_check
    private final Map<String, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();
    private final Set<String> preStartNotifiedSessions = ConcurrentHashMap.newKeySet();

    private static final long ONE_DAY_MS = 24L * 60 * 60 * 1000;
    private static final long SEVEN_DAYS_MS = 7L * ONE_DAY_MS;

    @PostConstruct
    public void init() {
        log.info("AuctionSchedulerService initializing...");
        try {
            syncAndScheduleAllAuctions();
        } catch (Exception e) {
            log.error("Failed initial syncAndScheduleAllAuctions", e);
        }
        // Periodic resync
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

            log.info("Processing auction {}: status={}, endTime={}", phien.getMaphiendg(), phien.getTrangthai(), phien.getThoigiankt());

            long startTime = phien.getThoigianbd().getTime();
            long endTime = phien.getThoigiankt().getTime();

            // Schedule notify BEFORE anything else
            schedulePreStartNotifyOnce(phien, now, startTime);

            // 1) Quá endTime
            if (now >= endTime) {
                if (phien.getTrangthai() != TrangThaiPhienDauGia.WAITING_FOR_PAYMENT
                        && phien.getTrangthai() != TrangThaiPhienDauGia.SUCCESS
                        && phien.getTrangthai() != TrangThaiPhienDauGia.FAILED
                        && phien.getTrangthai() != TrangThaiPhienDauGia.CANCELLED) {
                    endAuctionSafe(phien);
                } else {
                    cancelScheduledTask(phien.getMaphiendg());
                }
                // Reschedule payment check nếu WAITING_FOR_PAYMENT (vì task cũ mất sau restart)
                if (phien.getTrangthai() == TrangThaiPhienDauGia.WAITING_FOR_PAYMENT) {
                    // Fetch lại để đảm bảo relationship được load
                    Phiendaugia fresh = phiendaugiaRepository.findById(phien.getMaphiendg()).orElse(phien);
                    Phieuthanhtoan phieu = fresh.getPhieuThanhToan();
                    if (phieu != null && phieu.getTrangthai() == TrangThaiPhieuThanhToan.PAID) {
                        // Nếu phiếu đã PAID, trigger finalize ngay
                        log.info("Phiếu đã PAID cho phiên {}, trigger finalize ngay", fresh.getMaphiendg());
                        checkPaymentAndFinalize(fresh);
                    } else {
                        schedulePaymentCheck(fresh);
                    }
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
        if (phien.getTrangthai() == TrangThaiPhienDauGia.WAITING_FOR_PAYMENT
                || phien.getTrangthai() == TrangThaiPhienDauGia.SUCCESS
                || phien.getTrangthai() == TrangThaiPhienDauGia.FAILED) return;
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
            int participantCount = phien.getSlnguoithamgia();
            if (participantCount < 1) {
                log.warn("Cannot start auction {}: Not enough participants ({} < 1)", phien.getMaphiendg(), participantCount);
                phien.setTrangthai(TrangThaiPhienDauGia.FAILED);
                phiendaugiaRepository.save(phien);
                // Gửi email thất bại
                try {
                    emailService.sendAuctionEndEmail(phien.getTaiKhoan(), phien, "Không đủ số lượng người tham gia (tối thiểu 5 người).");
                } catch (Exception e) {
                    log.error("Failed to send end email for failed auction {}: {}", phien.getMaphiendg(), e.getMessage());
                }
                return;
            }

            if (phien.getTrangthai() == TrangThaiPhienDauGia.IN_PROGRESS) return;
            phien.setTrangthai(TrangThaiPhienDauGia.IN_PROGRESS);
            phiendaugiaRepository.save(phien);
            log.info("▶ Phiên {} → IN_PROGRESS ({} participants)", phien.getMaphiendg(), participantCount);
        } catch (Exception e) {
            log.error("Start auction failed {}: {}", phien.getMaphiendg(), e.getMessage());
        }
    }

    private void endAuction(Phiendaugia phien) {
        try {
            List<Phientragia> validBids = phientragiaRepository.findByPhienDauGia_Maphiendg(phien.getMaphiendg());
            boolean hasValidBid = !validBids.isEmpty();
            BigDecimal highestBid = phientragiaRepository.findMaxSotienByPhienDauGia_Maphiendg(phien.getMaphiendg())
                    .orElse(BigDecimal.ZERO);
            BigDecimal giaTran = phien.getGiatran();
            boolean priceCondition = giaTran != null && highestBid.compareTo(giaTran.multiply(BigDecimal.valueOf(0.5))) > 0;

            String lydo = "";
            if (hasValidBid && priceCondition) {
                // Tạo phiếu thanh toán cho người thắng
                try {
                    Phieuthanhtoan phieu = phieuthanhtoanService.createForWinner(phien);
                    phien.setPhieuThanhToan(phieu); // Giả sử entity có setPhieuThanhToan
                } catch (Exception e) {
                    log.error("Failed to create payment ticket for {}: {}", phien.getMaphiendg(), e.getMessage());
                }

                phien.setTrangthai(TrangThaiPhienDauGia.WAITING_FOR_PAYMENT);
                phiendaugiaRepository.save(phien);
                try {
                    Phientragia winnerBid = phientragiaRepository.findByPhienDauGia_Maphiendg(phien.getMaphiendg())
                            .stream()
                            .max(Comparator.comparing(Phientragia::getSotien))
                            .orElse(null);
                    if (winnerBid != null && winnerBid.getTaiKhoan() != null) {
                        BigDecimal giaThang = winnerBid.getSotien();
                        emailService.sendAuctionWinEmail(winnerBid.getTaiKhoan(), phien, giaThang);
                        log.info("Đã gửi email thông báo thắng cho {} trong phiên {}", winnerBid.getTaiKhoan().getEmail(), phien.getMaphiendg());
                    }
                } catch (Exception e) {
                    log.error("Lỗi gửi email thắng cho phiên {}: {}", phien.getMaphiendg(), e.getMessage());
                }
                schedulePaymentCheck(phien);
                log.info("Phiên {} → WAITING_FOR_PAYMENT", phien.getMaphiendg());
                // Không gửi email ở đây vì chưa kết thúc, chờ thanh toán
            } else {
                phien.setTrangthai(TrangThaiPhienDauGia.FAILED);
                phiendaugiaRepository.save(phien);
                if (!hasValidBid) {
                    lydo = "Không có người tham gia trả giá.";
                } else {
                    lydo = "Giá cao nhất không đạt yêu cầu (phải > 50% giá trần).";
                }
                // Gửi email thất bại
                try {
                    emailService.sendAuctionEndEmail(phien.getTaiKhoan(), phien, lydo);
                } catch (Exception e) {
                    log.error("Failed to send end email for failed auction {}: {}", phien.getMaphiendg(), e.getMessage());
                }
                log.info("Phiên {} → FAILED ({})", phien.getMaphiendg(), lydo);
            }

            scheduledTasks.remove(phien.getMaphiendg() + "_start");
            scheduledTasks.remove(phien.getMaphiendg() + "_end");
            scheduledTasks.remove(phien.getMaphiendg() + "_notify");
        } catch (Exception e) {
            log.error("End auction failed {}: {}", phien.getMaphiendg(), e.getMessage());
        }
    }

    // Thêm method để schedule kiểm tra thanh toán sau 7 ngày từ endTime
    private void schedulePaymentCheck(Phiendaugia phien) {
        String key = phien.getMaphiendg() + "_payment_check";
        if (scheduledTasks.containsKey(key)) return;

        // Tính delay từ endTime + 7 ngày (đảm bảo chính xác)
        long endTime = phien.getThoigiankt().getTime();
        long paymentCheckTime = endTime + SEVEN_DAYS_MS;
        long now = System.currentTimeMillis();
        long delay = Math.max(0, paymentCheckTime - now);  // Nếu đã quá hạn, chạy ngay

        log.info("Scheduling payment check for {} at {} (delay={}ms)", phien.getMaphiendg(), paymentCheckTime, delay);

        ScheduledFuture<?> future = scheduler.schedule(() -> {
            try {
                checkPaymentAndFinalize(phien);
            } catch (Exception e) {
                log.error("Payment check error {}: {}", phien.getMaphiendg(), e.getMessage());
            } finally {
                scheduledTasks.remove(key);
            }
        }, Instant.ofEpochMilli(now + delay));

        scheduledTasks.put(key, future);
    }

    // Logic kiểm tra thanh toán và finalize trạng thái
    private void checkPaymentAndFinalize(Phiendaugia phien) {
        try {
            // Fetch lại phiên từ DB để đảm bảo dữ liệu mới nhất (bao gồm relationship)
            Phiendaugia fresh = phiendaugiaRepository.findById(phien.getMaphiendg()).orElse(phien);
            Phieuthanhtoan phieu = fresh.getPhieuThanhToan();

            log.info("Checking payment for session {}: phieu exists={}, status={}",
                    fresh.getMaphiendg(), phieu != null, phieu != null ? phieu.getTrangthai() : "null");

            boolean winnerPaid = phieu != null && phieu.getTrangthai() == TrangThaiPhieuThanhToan.PAID;

            if (winnerPaid) {
                fresh.setTrangthai(TrangThaiPhienDauGia.SUCCESS);
                phiendaugiaRepository.save(fresh);
                // Gửi email thành công
                try {
                    emailService.sendAuctionEndEmail(fresh.getTaiKhoan(), fresh, "Phiên đấu giá thành công. Người thắng đã thanh toán.");
                } catch (Exception e) {
                    log.error("Failed to send end email for successful auction {}: {}", fresh.getMaphiendg(), e.getMessage());
                }
                log.info("🎉 Phiên {} → SUCCESS (winner paid)", fresh.getMaphiendg());
            } else {
                List<Phientragia> bids = phientragiaRepository.findByPhienDauGia_Maphiendg(fresh.getMaphiendg());
                BigDecimal secondHighestBid = bids.stream()
                        .map(Phientragia::getSotien)
                        .sorted(Comparator.reverseOrder())
                        .skip(1)
                        .findFirst()
                        .orElse(BigDecimal.ZERO);

                BigDecimal giaTran = fresh.getGiatran();
                boolean secondPriceCondition = giaTran != null && secondHighestBid.compareTo(giaTran.multiply(BigDecimal.valueOf(0.5))) > 0;

                if (secondPriceCondition) {
                    log.info("🔄 Phiên {}: Transfer to second winner", fresh.getMaphiendg());
                    // Implement transfer và schedule lại nếu cần
                } else {
                    fresh.setTrangthai(TrangThaiPhienDauGia.FAILED);
                    phiendaugiaRepository.save(fresh);
                    try {
                        emailService.sendAuctionEndEmail(fresh.getTaiKhoan(), fresh, "Người thắng không thanh toán và không có người kế tiếp đủ điều kiện.");
                    } catch (Exception e) {
                        log.error("Failed to send end email for failed auction {}: {}", fresh.getMaphiendg(), e.getMessage());
                    }
                    log.info("❌ Phiên {} → FAILED (winner did not pay, no fallback)", fresh.getMaphiendg());
                }
            }
            phiendaugiaRepository.save(fresh);
        } catch (Exception e) {
            log.error("Finalize payment check failed {}: {}", phien.getMaphiendg(), e.getMessage());
        }
    }

    public void cancelScheduledTask(String maphiendg) {
        if (maphiendg == null) return;
        for (String suffix : new String[]{"_start", "_end", "_notify", "_payment_check"}) {
            String key = maphiendg + suffix;
            ScheduledFuture<?> f = scheduledTasks.remove(key);
            if (f != null) {
                try {
                    f.cancel(false);
                } catch (Exception ignored) {
                }
            }
        }
    }

    // Thêm method để hủy phiên (gọi từ controller khi quản trị viên hủy)
    public void cancelAuction(String maphiendg, String reason) {
        try {
            Phiendaugia phien = phiendaugiaRepository.findById(maphiendg).orElse(null);
            if (phien == null) return;

            if (phien.getTrangthai() != TrangThaiPhienDauGia.CANCELLED) {
                phien.setTrangthai(TrangThaiPhienDauGia.CANCELLED);
                phiendaugiaRepository.save(phien);
                cancelScheduledTask(maphiendg);
                // Gửi email hủy
                try {
                    emailService.sendAuctionEndEmail(phien.getTaiKhoan(), phien, "Phiên đấu giá đã bị hủy: " + reason);
                } catch (Exception e) {
                    log.error("Failed to send end email for cancelled auction {}: {}", maphiendg, e.getMessage());
                }
                log.info("🚫 Phiên {} → CANCELLED (reason: {})", maphiendg, reason);
            }
        } catch (Exception e) {
            log.error("Cancel auction failed {}: {}", maphiendg, e.getMessage());
        }
    }

    private void schedulePreStartNotifyOnce(Phiendaugia phien, long now, long startTime) throws MessagingException, IOException {
        if (preStartNotifiedSessions.contains(phien.getMaphiendg())) return;
        long diff = startTime - now;

        if (diff <= 0) {
            return;
        }

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

        doNotifyPreStart(phien);
    }

    private void doNotifyPreStart(Phiendaugia phien) throws MessagingException, IOException {
        if (preStartNotifiedSessions.contains(phien.getMaphiendg())) return;
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