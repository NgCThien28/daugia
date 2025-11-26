package com.example.daugia.service;

import com.example.daugia.core.enums.TrangThaiPhienDauGia;
import com.example.daugia.core.enums.TrangThaiPhieuThanhToan;
import com.example.daugia.core.enums.TrangThaiPhieuThanhToanTienCoc;
import com.example.daugia.entity.Phiendaugia;
import com.example.daugia.entity.Phientragia;
import com.example.daugia.entity.Phieuthanhtoan;
import com.example.daugia.entity.Phieuthanhtoantiencoc;
import com.example.daugia.repository.PhiendaugiaRepository;
import com.example.daugia.repository.PhieuthanhtoanRepository;
import com.example.daugia.repository.PhieuthanhtoantiencocRepository;
import com.example.daugia.repository.PhientragiaRepository;
import jakarta.annotation.PostConstruct;
import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Timestamp;
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
    private PhieuthanhtoanService phieuthanhtoanService;
    @Autowired
    private PhieuthanhtoanRepository phieuthanhtoanRepository;// Inject service mới
    @Autowired
    private EmailService emailService;

    // _start, _end, _notify, _payment_check
    private final Map<String, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();
    private final Set<String> preStartNotifiedSessions = ConcurrentHashMap.newKeySet();

    // per-auction locks to avoid concurrent finalize for same auction
    private final Map<String, Object> finalizationLocks = new ConcurrentHashMap<>();

    private static final long ONE_DAY_MS = 24L * 60 * 60 * 1000;
    private static final long SEVEN_DAYS_MS = 7L * ONE_DAY_MS;

    @PostConstruct
    public void init() {
        log.info("Khoi tao AuctionSchedulerService...");
        try {
            syncAndScheduleAllAuctions();
        } catch (Exception e) {
            log.error("Loi khoi tao dau tien syncAndScheduleAllAuctions", e);
        }
        // Periodic resync
        scheduler.scheduleAtFixedRate(this::safeSyncAndScheduleAllAuctions, 30 * 60_000L);
        log.info("Khoi tao AuctionSchedulerService xong (resync 30m).");
    }

    private void safeSyncAndScheduleAllAuctions() {
        try {
            syncAndScheduleAllAuctions();
        } catch (Exception e) {
            log.error("Loi trong resync dinh ky", e);
        }
    }

    private void syncAndScheduleAllAuctions() throws MessagingException, IOException {
        List<Phiendaugia> all = phiendaugiaRepository.findAll();
        long now = System.currentTimeMillis();
        log.info("Dong bo {} phien dau gia. Bay gio={}", all.size(), now);

        for (Phiendaugia phien : all) {
            if (phien == null || phien.getMaphiendg() == null
                    || phien.getThoigianbd() == null || phien.getThoigiankt() == null) {
                continue;
            }

            log.info("Xu ly phien {}: trang thai={}, thoi gian ket thuc={}", phien.getMaphiendg(), phien.getTrangthai(), phien.getThoigiankt());

            long startTime = phien.getThoigianbd().getTime();
            long endTime = phien.getThoigiankt().getTime();

            // Schedule notify BEFORE anything else
            schedulePreStartNotifyOnce(phien, now, startTime);

            // 1) Qua thoi gian ket thuc
            if (now >= endTime) {
                if (phien.getTrangthai() != TrangThaiPhienDauGia.WAITING_FOR_PAYMENT
                        && phien.getTrangthai() != TrangThaiPhienDauGia.SUCCESS
                        && phien.getTrangthai() != TrangThaiPhienDauGia.FAILED
                        && phien.getTrangthai() != TrangThaiPhienDauGia.CANCELLED) {
                    endAuctionSafe(phien);
                } else {
                    cancelScheduledTask(phien.getMaphiendg());
                }
                // Reschedule payment check neu WAITING_FOR_PAYMENT (vi task cu mat sau restart)
                if (phien.getTrangthai() == TrangThaiPhienDauGia.WAITING_FOR_PAYMENT) {
                    Phiendaugia fresh = phiendaugiaRepository.findById(phien.getMaphiendg()).orElse(phien);
                    Phieuthanhtoan phieu = fresh.getPhieuThanhToan();
                    if (phieu != null && phieu.getTrangthai() == TrangThaiPhieuThanhToan.PAID) {
                        log.info("Phieu da thanh toan cho phien {}, kich hoat ket thuc ngay", fresh.getMaphiendg());
                        checkPaymentAndFinalize(fresh);
                    } else {
                        schedulePaymentCheck(fresh);
                    }
                }
                continue;
            }

            // 2) Dang trong phien
            if (now >= startTime) {
                if (phien.getTrangthai() == TrangThaiPhienDauGia.NOT_STARTED
                        || phien.getTrangthai() == TrangThaiPhienDauGia.APPROVED) {
                    startAuctionSafe(phien);
                }
                scheduleEndOnce(phien);
                continue;
            }

            // 3) Chua toi gio bat dau
            if (phien.getTrangthai() == TrangThaiPhienDauGia.APPROVED) {
                phien.setTrangthai(TrangThaiPhienDauGia.NOT_STARTED);
                try {
                    phiendaugiaRepository.save(phien);
                } catch (Exception e) {
                    log.warn("Khong the danh dau NOT_STARTED cho {}: {}", phien.getMaphiendg(), e.getMessage());
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
                log.error("Loi bat dau theo lich {}: {}", phien.getMaphiendg(), e.getMessage());
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
                log.error("Loi ket thuc theo lich {}: {}", phien.getMaphiendg(), e.getMessage());
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
                log.warn("Khong the bat dau phien {}: Khong du nguoi tham gia ({} < 1)", phien.getMaphiendg(), participantCount);
                phien.setTrangthai(TrangThaiPhienDauGia.FAILED);
                phiendaugiaRepository.save(phien);
                // Gui email that bai
                try {
                    emailService.sendAuctionEndEmail(phien.getTaiKhoan(), phien, "Khong du so luong nguoi tham gia (to thieu 5 nguoi).");
                } catch (Exception e) {
                    log.error("Loi gui email that bai cho phien {}: {}", phien.getMaphiendg(), e.getMessage());
                }
                return;
            }

            if (phien.getTrangthai() == TrangThaiPhienDauGia.IN_PROGRESS) return;
            phien.setTrangthai(TrangThaiPhienDauGia.IN_PROGRESS);
            phiendaugiaRepository.save(phien);
            log.info("▶ Phien {} → IN_PROGRESS ({} nguoi tham gia)", phien.getMaphiendg(), participantCount);
        } catch (Exception e) {
            log.error("Bat dau phien that bai {}: {}", phien.getMaphiendg(), e.getMessage());
        }
    }

    private void endAuction(Phiendaugia phien) {
        try {
            List<Phientragia> validBids = phientragiaRepository.findByPhienDauGia_Maphiendg(phien.getMaphiendg());
            boolean hasValidBid = !validBids.isEmpty();
            BigDecimal highestBid = phientragiaRepository.findMaxSotienByPhienDauGia_Maphiendg(phien.getMaphiendg())
                    .orElse(BigDecimal.ZERO);

            String lydo = "";
            if (hasValidBid) {
                // Tao phieu thanh toan cho nguoi thang
                try {
                    Phieuthanhtoan phieu = phieuthanhtoanService.createForWinner(phien);
                    phien.setPhieuThanhToan(phieu);
                } catch (Exception e) {
                    log.error("Loi tao phieu thanh toan cho {}: {}", phien.getMaphiendg(), e.getMessage());
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
                        log.info("Da gui email thong bao thang cho {} trong phien {}", winnerBid.getTaiKhoan().getEmail(), phien.getMaphiendg());
                    }
                } catch (Exception e) {
                    log.error("Loi gui email thang cho phien {}: {}", phien.getMaphiendg(), e.getMessage());
                }
                schedulePaymentCheck(phien);
                log.info("Phien {} → WAITING_FOR_PAYMENT", phien.getMaphiendg());
            } else {
                phien.setTrangthai(TrangThaiPhienDauGia.FAILED);
                phiendaugiaRepository.save(phien);
                lydo = "Khong co nguoi tham gia tra gia.";
                // Gui email that bai
                try {
                    emailService.sendAuctionEndEmail(phien.getTaiKhoan(), phien, lydo);
                } catch (Exception e) {
                    log.error("Loi gui email that bai cho phien {}: {}", phien.getMaphiendg(), e.getMessage());
                }
                log.info("Phien {} → FAILED ({})", phien.getMaphiendg(), lydo);
            }

            scheduledTasks.remove(phien.getMaphiendg() + "_start");
            scheduledTasks.remove(phien.getMaphiendg() + "_end");
            scheduledTasks.remove(phien.getMaphiendg() + "_notify");
        } catch (Exception e) {
            log.error("Ket thuc phien that bai {}: {}", phien.getMaphiendg(), e.getMessage());
        }
    }

    private void schedulePaymentCheck(Phiendaugia phien) {
        Phieuthanhtoan phieu = phien.getPhieuThanhToan();
        if (phieu == null) return;

        String key = phien.getMaphiendg() + "_payment_check";
        long paymentCheckTime = phieu.getThoigianthanhtoan().getTime();
        long now = System.currentTimeMillis();
        long delay = Math.max(0, paymentCheckTime - now);

        log.info("Len lich kiem tra thanh toan cho {} vao {} (delay={}ms)", phien.getMaphiendg(), paymentCheckTime, delay);

        scheduledTasks.computeIfAbsent(key, k -> scheduler.schedule(() -> {
            try {
                try {
                    Phiendaugia fresh = phiendaugiaRepository.findById(phien.getMaphiendg()).orElse(phien);
                    checkPaymentAndFinalize(fresh);
                } catch (Exception e) {
                    log.error("Loi kiem tra thanh toan trong {}: {}", phien.getMaphiendg(), e.getMessage());
                }
            } catch (Exception e) {
                log.error("Loi kiem tra thanh toan {}: {}", phien.getMaphiendg(), e.getMessage());
            } finally {
                scheduledTasks.remove(k);
            }
        }, Instant.ofEpochMilli(now + delay)));
    }

    @Transactional
    private void checkPaymentAndFinalize(Phiendaugia phien) {
        String maphiendg = phien.getMaphiendg();
        Object lock = finalizationLocks.computeIfAbsent(maphiendg, k -> new Object());
        synchronized (lock) {
            try {
                Phiendaugia fresh = phiendaugiaRepository.findById(maphiendg).orElse(phien);
                if (fresh.getTrangthai() != TrangThaiPhienDauGia.WAITING_FOR_PAYMENT) {
                    log.info("Bo qua ket thuc cho {} vi trang thai={}", maphiendg, fresh.getTrangthai());
                    return;
                }

                Phieuthanhtoan phieu = fresh.getPhieuThanhToan();
                log.info("Kiem tra thanh toan cho phien {}: phieu ton tai={}, trang thai={}",
                        fresh.getMaphiendg(), phieu != null, phieu != null ? phieu.getTrangthai() : "null");

                boolean winnerPaid = phieu != null && phieu.getTrangthai() == TrangThaiPhieuThanhToan.PAID;
                log.info("Nguoi thang da thanh toan: {}", winnerPaid);
                if (phieu != null) {
                    log.info("Trang thai phieu: {}, het han: {}", phieu.getTrangthai(), phieu.getThoigianthanhtoan().before(new java.util.Date()));
                }
                if (winnerPaid) {
                    fresh.setTrangthai(TrangThaiPhienDauGia.SUCCESS);
                    phiendaugiaRepository.save(fresh);
                    try {
                        emailService.sendAuctionEndEmail(fresh.getTaiKhoan(), fresh, "Phien dau gia thanh cong. Nguoi thang da thanh toan.");
                    } catch (Exception e) {
                        log.error("Loi gui email thanh cong cho phien {}: {}", fresh.getMaphiendg(), e.getMessage());
                    }
                    log.info("Phien {} → SUCCESS (nguoi thang da thanh toan)", fresh.getMaphiendg());
                } else {
                    List<Phientragia> bids = phientragiaRepository.findByPhienDauGia_Maphiendg(fresh.getMaphiendg());
                    log.info("So luot tra gia: {}", bids.size());
                    if (!bids.isEmpty()) {
                        BigDecimal highestBid = bids.stream().map(Phientragia::getSotien).max(Comparator.naturalOrder()).orElse(BigDecimal.ZERO);
                        log.info("Gia cao nhat: {}", highestBid);
                        Phientragia highestBidder = bids.stream().filter(b -> b.getSotien().equals(highestBid)).findFirst().orElse(null);
                        if (highestBidder != null && highestBidder.getTaiKhoan() != null) {
                            log.info("Nguoi tra gia cao nhat: {}", highestBidder.getTaiKhoan().getMatk());
                        }
                        if (phieu != null && phieu.getTaiKhoan() != null) {
                            log.info("Tai khoan phieu: {}", phieu.getTaiKhoan().getMatk());
                            boolean hasTransferredToSecond = !phieu.getTaiKhoan().getMatk().equals(
                                    bids.stream().filter(b -> b.getSotien().equals(highestBid)).findFirst().orElse(new Phientragia()).getTaiKhoan().getMatk()
                            );
                            log.info("Da chuyen cho nguoi thu hai: {}", hasTransferredToSecond);
                            if (hasTransferredToSecond) {
                                if (phieu.getThoigianthanhtoan().before(new java.util.Date())) {
                                    fresh.setTrangthai(TrangThaiPhienDauGia.FAILED);

                                    phiendaugiaRepository.save(fresh);
                                    try {
                                        emailService.sendAuctionEndEmail(fresh.getTaiKhoan(), fresh, "Nguoi thang thu hai khong thanh toan.");
                                    } catch (Exception e) {
                                        log.error("Loi gui email that bai cho phien {}: {}", fresh.getMaphiendg(), e.getMessage());
                                    }
                                    log.info("❌ Phien {} → FAILED (nguoi thang thu hai khong thanh toan)", fresh.getMaphiendg());
                                    return;
                                } else {
                                    log.info("Phien {} da chuyen, cho nguoi thu hai thanh toan trong thoi gian gia han.", fresh.getMaphiendg());
                                    return;
                                }
                            }
                        }
                    }

                    BigDecimal secondHighestBid = bids.stream()
                            .map(Phientragia::getSotien)
                            .sorted(Comparator.reverseOrder())
                            .skip(1)
                            .findFirst()
                            .orElse(BigDecimal.ZERO);

                    if (bids.size() > 1) {
                        Phientragia secondWinnerBid = bids.stream()
                                .sorted(Comparator.comparing(Phientragia::getSotien).reversed())
                                .skip(1)
                                .findFirst()
                                .orElse(null);

                        if (secondWinnerBid != null) {
                            if (phieu == null) throw new AssertionError();
                            if (phieu.getTaiKhoan() == null) throw new AssertionError();
                            emailService.sendAuctionCancelWinEmail(phieu.getTaiKhoan(), fresh);
                            phieu.setTaiKhoan(secondWinnerBid.getTaiKhoan());
                            phieu.setSotien(secondHighestBid);
                            phieu.setThoigianthanhtoan(new Timestamp(System.currentTimeMillis() + SEVEN_DAYS_MS));
                            phieuthanhtoanRepository.save(phieu);
                            fresh.setGiacaonhatdatduoc(secondHighestBid);
                            phiendaugiaRepository.save(fresh);
                            emailService.sendAuctionTransferEmail(secondWinnerBid.getTaiKhoan(), fresh, secondHighestBid);
                            schedulePaymentCheck(fresh);
                            log.info("🔄 Phien {}: Chuyen cho nguoi thang thu hai {}", fresh.getMaphiendg(), secondWinnerBid.getTaiKhoan().getEmail());
                        } else {
                            log.warn("Khong tim thay nguoi thang thu hai cho {}", fresh.getMaphiendg());
                            fresh.setTrangthai(TrangThaiPhienDauGia.FAILED);
                            phiendaugiaRepository.save(fresh);
                            try {
                                emailService.sendAuctionEndEmail(fresh.getTaiKhoan(), fresh, "Nguoi thang khong thanh toan va khong co nguoi ke tiep.");
                            } catch (Exception e) {
                                log.error("Loi gui email that bai cho phien {}: {}", fresh.getMaphiendg(), e.getMessage());
                            }
                        }
                    } else {
                        fresh.setTrangthai(TrangThaiPhienDauGia.FAILED);
                        phiendaugiaRepository.save(fresh);
                        try {
                            emailService.sendAuctionEndEmail(fresh.getTaiKhoan(), fresh, "Nguoi thang khong thanh toan va khong co nguoi ke tiep du dieu kien.");
                        } catch (Exception e) {
                            log.error("Loi gui email that bai cho phien {}: {}", fresh.getMaphiendg(), e.getMessage());
                        }
                        log.info("❌ Phien {} → FAILED (nguoi thang khong thanh toan, khong co thay the)", fresh.getMaphiendg());
                    }
                }
            } catch (Exception e) {
                log.error("Ket thuc kiem tra thanh toan that bai {}: {}", phien.getMaphiendg(), e.getMessage());
            } finally {
                finalizationLocks.remove(maphiendg);
            }
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

    public void cancelAuction(String maphiendg, String reason) {
        try {
            Phiendaugia phien = phiendaugiaRepository.findById(maphiendg).orElse(null);
            if (phien == null) return;

            if (phien.getTrangthai() != TrangThaiPhienDauGia.CANCELLED) {
                phien.setTrangthai(TrangThaiPhienDauGia.CANCELLED);
                phiendaugiaRepository.save(phien);
                cancelScheduledTask(maphiendg);
                try {
                    emailService.sendAuctionEndEmail(phien.getTaiKhoan(), phien, "Phien dau gia da bi huy: " + reason);
                } catch (Exception e) {
                    log.error("Loi gui email huy cho phien {}: {}", maphiendg, e.getMessage());
                }
                log.info("🚫 Phien {} → CANCELLED (ly do: {})", maphiendg, reason);
            }
        } catch (Exception e) {
            log.error("Huy phien that bai {}: {}", maphiendg, e.getMessage());
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
                    log.error("Loi thong bao theo lich {}: {}", phien.getMaphiendg(), e.getMessage());
                } finally {
                    scheduledTasks.remove(key);
                }
            }, Instant.ofEpochMilli(notifyAt));

            scheduledTasks.put(key, future);
            return;
        }

        doNotifyPreStart(phien);
    }

    private void doNotifyPreStart(Phiendaugia phien) throws MessagingException, IOException {
        if (preStartNotifiedSessions.contains(phien.getMaphiendg())) return;
        Phiendaugia fresh = phiendaugiaRepository.findById(phien.getMaphiendg()).orElse(phien);

        if (!(fresh.getTrangthai() == TrangThaiPhienDauGia.NOT_STARTED
                || fresh.getTrangthai() == TrangThaiPhienDauGia.APPROVED)) {
            return;
        }

        List<Phieuthanhtoantiencoc> paid =
                phieuthanhtoantiencocRepository.findByPhienDauGia_MaphiendgAndTrangthai(
                        fresh.getMaphiendg(), TrangThaiPhieuThanhToanTienCoc.PAID);

        log.info("Gui thong bao bat dau truoc (24h) cho {} -> {} phieu coc da thanh toan",
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
            log.warn("scheduleNewOrApprovedAuction: {} khong tim thay hoac thieu thoi gian", maphiendg);
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
                log.warn("Loi luu trang thai {}: {}", maphiendg, e.getMessage());
            }
        }
        scheduleStartOnce(phien);
        scheduleEndOnce(phien);
    }
}