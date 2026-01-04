package com.example.daugia.service;

import com.example.daugia.core.enums.TrangThaiPhienDauGia;
import com.example.daugia.core.enums.TrangThaiPhieuThanhToan;
import com.example.daugia.core.enums.TrangThaiPhieuThanhToanTienCoc;
import com.example.daugia.dto.request.ThongBaoCreationRequest;
import com.example.daugia.entity.Phiendaugia;
import com.example.daugia.entity.Phientragia;
import com.example.daugia.entity.Phieuthanhtoan;
import com.example.daugia.entity.Phieuthanhtoantiencoc;
import com.example.daugia.exception.NotFoundException;
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
import java.util.Date;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
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
//Lap lich, xu ly trang thai, thong bao + email, dong bo du lieu dtb
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
    private PhieuthanhtoanRepository phieuthanhtoanRepository;
    @Autowired
    private EmailService emailService;
    @Autowired
    private ThongbaoService thongbaoService;

    //Key: _start, _end, _notify, _payment_check
    private final Map<String, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();
    private final Set<String> preStartNotifiedSessions = ConcurrentHashMap.newKeySet();

    //Khoa rieng cho phien tranh hoan tat dong thoi cung 1 phien
    private final Map<String, Object> finalizationLocks = new ConcurrentHashMap<>();

    private static final long ONE_DAY_MS = 24L * 60 * 60 * 1000;
    private static final long SEVEN_DAYS_MS = 7L * ONE_DAY_MS;

    //Khoi tao
    @PostConstruct
    public void init() {
        log.info("Khoi tao AuctionSchedulerService...");
        try {
            syncAndScheduleAllAuctions();
        } catch (Exception e) {
            log.error("Loi khoi tao dau tien syncAndScheduleAllAuctions", e);
        }
        //Dong bo moi 30 phut
        Date startTime = new Date(System.currentTimeMillis() + 30 * 60_000L);
        scheduler.scheduleAtFixedRate(this::safeSyncAndScheduleAllAuctions, startTime, 30 * 60_000L);
        log.info("Khoi tao AuctionSchedulerService xong.");
    }

    private void safeSyncAndScheduleAllAuctions() {
        try {
            syncAndScheduleAllAuctions();
        } catch (Exception e) {
            log.error("Loi trong dong bo dinh ky ", e);
        }
    }

    //Dong bo hoa phien dau gia
    private void syncAndScheduleAllAuctions() throws MessagingException, IOException {
        List<TrangThaiPhienDauGia> excludedStatuses = List.of(
                TrangThaiPhienDauGia.SUCCESS,
                TrangThaiPhienDauGia.FAILED,
                TrangThaiPhienDauGia.CANCELLED,
                TrangThaiPhienDauGia.PENDING_APPROVAL,
                TrangThaiPhienDauGia.NOT_REQUESTED
        );
        List<Phiendaugia> all = phiendaugiaRepository.findActiveAuctions(excludedStatuses);
        long now = System.currentTimeMillis();
        log.debug("Dong bo {} phien dau gia. Bay gio={}", all.size(), now);

        for (Phiendaugia phien : all) {
            try {
                //Kiem tra sai sot du lieu
                if (phien == null || phien.getMaphiendg() == null
                        || phien.getThoigianbd() == null || phien.getThoigiankt() == null) {
                    continue;
                }

                log.debug("Xu ly phien {}: trang thai={}, thoi gian ket thuc={}", phien.getMaphiendg(), phien.getTrangthai(), phien.getThoigiankt());

                long startTime = phien.getThoigianbd().getTime();
                long endTime = phien.getThoigiankt().getTime();

                //Thong bao truoc khi bat dau phien
                schedulePreStartNotifyOnce(phien, now, startTime);

                //1. Qua thoi gian ket thuc
                if (now >= endTime) {
                    //Ket thuc phien neu phien IN_PROGRESS, NOT_STARTED
                    if (phien.getTrangthai() != TrangThaiPhienDauGia.WAITING_FOR_PAYMENT
                            && phien.getTrangthai() != TrangThaiPhienDauGia.SUCCESS
                            && phien.getTrangthai() != TrangThaiPhienDauGia.FAILED
                            && phien.getTrangthai() != TrangThaiPhienDauGia.CANCELLED) {
                        endAuctionSafe(phien);
                    } else {
                        //Huy toan bo lich khong can thiet
                        cancelScheduledTask(phien.getMaphiendg());
                    }
                    //Len lich lai kt thanh toan khi khoi dong BE neu phien WAITING_FOR_PAYMENT
                    if (phien.getTrangthai() == TrangThaiPhienDauGia.WAITING_FOR_PAYMENT) {
                        Phiendaugia fresh = phiendaugiaRepository.findByIdWithPhieuThanhToan(phien.getMaphiendg()).orElse(phien);
                        Phieuthanhtoan phieu = getActivePhieu(fresh);
                        if (phieu != null && phieu.getTrangthai() == TrangThaiPhieuThanhToan.PAID) {
                            log.info("Phieu da thanh toan cho phien {}, kich hoat ket thuc ngay", fresh.getMaphiendg());
                            checkPaymentAndFinalize(fresh);
                        } else {
                            schedulePaymentCheck(fresh);
                        }
                    }
                    continue;
                }

                //2. Dang trong phien, lap lich ket thuc phien
                if (now >= startTime) {
                    if (phien.getTrangthai() == TrangThaiPhienDauGia.NOT_STARTED
                            || phien.getTrangthai() == TrangThaiPhienDauGia.APPROVED) {
                        startAuctionSafe(phien);
                    }
                    scheduleEndOnce(phien);
                    continue;
                }

                //3. Chua toi gio bat dau, lap lich bat dau va ket thuc phien
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
            } catch (Exception e) {
                log.error("Loi xu ly phien {}: {}", phien.getMaphiendg(), e.getMessage());
            }
        }
    }

    //Bat dau lai phien khi khoi dong BE
    //Loai bo lich cu trong danh sach va huy lich cu
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

    //Ket thuc phien khi khoi dong BE
    //Loai bo lich cu trong danh sach va huy lich cu
    private void endAuctionSafe(Phiendaugia phien) {
        cancelScheduledTask(phien.getMaphiendg());
        if (phien.getTrangthai() == TrangThaiPhienDauGia.WAITING_FOR_PAYMENT
                || phien.getTrangthai() == TrangThaiPhienDauGia.SUCCESS
                || phien.getTrangthai() == TrangThaiPhienDauGia.FAILED) return;
        endAuction(phien);
    }

    //Len lich bat dau cho phien
    private void scheduleStartOnce(Phiendaugia phien) {
        String key = phien.getMaphiendg() + "_start";
        //Kiem tra phien duoc lap lich hay chua
        if (scheduledTasks.containsKey(key)) return;

        //Thoi gian cho <=0 khong lap lich
        long delay = phien.getThoigianbd().getTime() - System.currentTimeMillis();
        if (delay <= 0) return;

        //Len lich
        ScheduledFuture<?> future = scheduler.schedule(() -> {
            try {
                Phiendaugia fresh = phiendaugiaRepository.findByIdWithPhieuThanhToan(phien.getMaphiendg()).orElse(phien);
                startAuctionSafe(fresh);
            } catch (Exception e) {
                log.error("Loi bat dau theo lich {}: {}", phien.getMaphiendg(), e.getMessage());
            } finally {
                scheduledTasks.remove(key);
            }
        }, Instant.ofEpochMilli(System.currentTimeMillis() + delay));

        scheduledTasks.put(key, future);
    }

    //Len lich ket thuc phien
    private void scheduleEndOnce(Phiendaugia phien) {
        String key = phien.getMaphiendg() + "_end";
        //Kiem tra phien duoc lap lich hay chua
        if (scheduledTasks.containsKey(key)) return;

        //Thoi gian cho <=0 khong lap lich
        long delay = phien.getThoigiankt().getTime() - System.currentTimeMillis();
        if (delay <= 0) {
            endAuctionSafe(phien);
            return;
        }

        //Lap lich
        ScheduledFuture<?> future = scheduler.schedule(() -> {
            try {
                Phiendaugia fresh = phiendaugiaRepository.findByIdWithPhieuThanhToan(phien.getMaphiendg()).orElse(phien);
                endAuctionSafe(fresh);
            } catch (Exception e) {
                log.error("Loi ket thuc theo lich {}: {}", phien.getMaphiendg(), e.getMessage());
            } finally {
                scheduledTasks.remove(key);
            }
        }, Instant.ofEpochMilli(System.currentTimeMillis() + delay));

        scheduledTasks.put(key, future);
    }

    //Bat dau phien
    private void startAuction(Phiendaugia phien) {
        try {
            int participantCount = phien.getSlnguoithamgia();
            if (participantCount < 5) {
                log.warn("Khong the bat dau phien {}: Khong du nguoi tham gia ({} < 5)", phien.getMaphiendg(), participantCount);
                phien.setTrangthai(TrangThaiPhienDauGia.FAILED);
                phiendaugiaRepository.save(phien);

                List<Phieuthanhtoantiencoc> allDeposits = phieuthanhtoantiencocRepository.findByPhienDauGia_Maphiendg(phien.getMaphiendg());
                for (Phieuthanhtoantiencoc p : allDeposits) {
                    if (p.getTrangthai() == TrangThaiPhieuThanhToanTienCoc.PAID) {
                        p.setTrangthai(TrangThaiPhieuThanhToanTienCoc.REFUNDED);
                        phieuthanhtoantiencocRepository.save(p);
                        createNotification(p.getTaiKhoan().getEmail(), "Hoàn tiền cọc", "Tiền cọc của bạn đã được hoàn lại do phiên đấu giá " + phien.getMaphiendg() + " với sản phẩm là " + phien.getSanPham().getTensp() + " thất bại.");
                    }
                }
                preStartNotifiedSessions.remove(phien.getMaphiendg());
                //Gui email that bai
                try {
                    emailService.sendAuctionEndEmail(phien.getTaiKhoan(), phien, "Không đủ số lượng người tham gia (tối thiểu 5 người).");
                    createEndNotification(phien, "Không đủ số lượng người tham gia.", false);
                } catch (Exception e) {
                    log.error("Loi gui email that bai cho phien {}: {}", phien.getMaphiendg(), e.getMessage());
                }
                return;
            }
            //Phien chua dien ra, cap nhat trang thai dang dien ra
            if (phien.getTrangthai() == TrangThaiPhienDauGia.IN_PROGRESS) return;
            phien.setTrangthai(TrangThaiPhienDauGia.IN_PROGRESS);
            phiendaugiaRepository.save(phien);
            log.debug("Phien {} → IN_PROGRESS ({} nguoi tham gia)", phien.getMaphiendg(), participantCount);
        } catch (Exception e) {
            log.error("Bat dau phien that bai {}: {}", phien.getMaphiendg(), e.getMessage());
        }
    }

    //Ket thuc phien
    @Transactional
    private void endAuction(Phiendaugia phien) {
        try {
            List<Phientragia> validBids = phientragiaRepository.findByPhienDauGia_Maphiendg(phien.getMaphiendg());
            boolean hasValidBid = !validBids.isEmpty();
            String lydo = "";

            if (hasValidBid) {
                //Tao phieu thanh toan cho nguoi thang
                try {
                    Phieuthanhtoan phieu = phieuthanhtoanService.createForWinner(phien);
                    phien.getPhieuThanhToan().add(phieu);
                } catch (Exception e) {
                    log.error("Loi tao phieu thanh toan cho {}: {}", phien.getMaphiendg(), e.getMessage());
                }
                phien.setTrangthai(TrangThaiPhienDauGia.WAITING_FOR_PAYMENT);
                phiendaugiaRepository.save(phien);
                List<Phieuthanhtoantiencoc> allDeposits = phieuthanhtoantiencocRepository.findByPhienDauGia_Maphiendg(phien.getMaphiendg());

                //Xac dinh nguoi thang dau tien
                Phientragia firstWinnerBid = validBids.stream()
                        .max(Comparator.comparing(Phientragia::getSotien))
                        .orElse(null);
                String firstWinnerId = (firstWinnerBid != null && firstWinnerBid.getTaiKhoan() != null) ? firstWinnerBid.getTaiKhoan().getMatk() : null;
                log.debug("Nguoi tra gia cao nhat (nguoi thang dau tien) trong phien {}: {}", phien.getMaphiendg(), firstWinnerId);
                //Dang hoan coc, tru nguoi thang
                for (Phieuthanhtoantiencoc p : allDeposits) {
                    if (p.getTrangthai() == TrangThaiPhieuThanhToanTienCoc.PAID && !p.getTaiKhoan().getMatk().equals(firstWinnerId)) {
                        p.setTrangthai(TrangThaiPhieuThanhToanTienCoc.REFUNDING);
                        phieuthanhtoantiencocRepository.save(p);
                        createNotification(p.getTaiKhoan().getEmail(), "Đang tiến hành hoàn cọc",
                                "Tiền cọc từ phiên " + phien.getMaphiendg() +
                                        " với sản phẩm là " + phien.getSanPham().getTensp() +
                                        " của bạn đang được xử lý hoàn lại.");
                    }
                }
                //Gui mail thong bao cho nguoi thang
                try {
                    Phientragia winnerBid = phientragiaRepository.findByPhienDauGia_Maphiendg(phien.getMaphiendg())
                            .stream()
                            .max(Comparator.comparing(Phientragia::getSotien))
                            .orElse(null);
                    if (winnerBid != null && winnerBid.getTaiKhoan() != null) {
                        BigDecimal giaThang = winnerBid.getSotien();
                        emailService.sendAuctionWinEmail(winnerBid.getTaiKhoan(), phien, giaThang);
                        createWinNotification(winnerBid, phien, giaThang);
                    }
                } catch (Exception e) {
                    log.error("Loi gui email thang cho phien {}: {}", phien.getMaphiendg(), e.getMessage());
                }
                schedulePaymentCheck(phien);
                log.info("Phien {} → WAITING_FOR_PAYMENT", phien.getMaphiendg());
            }
            //Khong ai tra gia
            else {
                phien.setTrangthai(TrangThaiPhienDauGia.FAILED);
                phiendaugiaRepository.save(phien);
                lydo = "Không có người tham gia trả giá.";
                //Hoan tien coc
                List<Phieuthanhtoantiencoc> allDeposits = phieuthanhtoantiencocRepository.findByPhienDauGia_Maphiendg(phien.getMaphiendg());
                for (Phieuthanhtoantiencoc p : allDeposits) {
                    if (p.getTrangthai() == TrangThaiPhieuThanhToanTienCoc.PAID) {
                        p.setTrangthai(TrangThaiPhieuThanhToanTienCoc.REFUNDED);
                        phieuthanhtoantiencocRepository.save(p);
                        createNotification(p.getTaiKhoan().getEmail(), "Hoàn tiền cọc",
                                "Tiền cọc của bạn đã được hoàn lại do phiên đấu giá " +
                                        phien.getMaphiendg() + " với sản phẩm là " +
                                        phien.getSanPham().getTensp() + " thất bại.");
                    }
                }
                //Thong bao email cho chu phien
                try {
                    emailService.sendAuctionEndEmail(phien.getTaiKhoan(), phien, lydo);
                } catch (Exception e) {
                    log.error("Loi gui email that bai cho phien {}: {}", phien.getMaphiendg(), e.getMessage());
                }
                log.info("Phien {} → FAILED ({})", phien.getMaphiendg(), lydo);
            }
            //xoa tat ca lich nhiem vu, xoa phien khoi danh sach da thong bao
            scheduledTasks.remove(phien.getMaphiendg() + "_start");
            scheduledTasks.remove(phien.getMaphiendg() + "_end");
            scheduledTasks.remove(phien.getMaphiendg() + "_notify");
            preStartNotifiedSessions.remove(phien.getMaphiendg());
        } catch (Exception e) {
            log.error("Ket thuc phien that bai {}: {}", phien.getMaphiendg(), e.getMessage());
        }
    }

    //Len lich kiem tra thanh toan
    private void schedulePaymentCheck(Phiendaugia phien) {
        //Lay phieu thanh toan moi nhat cua phien
        Phieuthanhtoan phieu = getActivePhieu(phien);
        if (phieu == null) return;

        String key = phien.getMaphiendg() + "_payment_check";
        long paymentCheckTime = phieu.getHanthanhtoan().getTime();
        long now = System.currentTimeMillis();
        long delay = Math.max(0, paymentCheckTime - now);

        log.debug("Len lich kiem tra thanh toan cho {} vao {} (delay={}ms)", phien.getMaphiendg(), paymentCheckTime, delay);

        //Len lich
        scheduledTasks.computeIfAbsent(key, k -> scheduler.schedule(() -> {
            try {
                try {
                    Phiendaugia fresh = phiendaugiaRepository.findByIdWithPhieuThanhToan(phien.getMaphiendg()).orElse(phien);
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

    //Kiem tra thanh toan
    @Transactional
    private void checkPaymentAndFinalize(Phiendaugia phien) {
        String maphiendg = phien.getMaphiendg();
        //Lay khoa hien tai, neu chua co thi tao moi
        Object lock = finalizationLocks.computeIfAbsent(maphiendg, k -> new Object());
        synchronized (lock) { //1 luong xu ly phien
            try {
                Phiendaugia fresh = phiendaugiaRepository.findByIdWithPhieuThanhToan(maphiendg).orElse(phien);
                if (fresh.getTrangthai() != TrangThaiPhienDauGia.WAITING_FOR_PAYMENT) {
                    log.debug("Bo qua ket thuc cho {} vi trang thai={}", maphiendg, fresh.getTrangthai());
                    return;
                }

                Phieuthanhtoan phieu = getActivePhieu(fresh);

                boolean winnerPaid = phieu != null && phieu.getTrangthai() == TrangThaiPhieuThanhToan.PAID;
                //Nguoi thang thanh toan
                if (winnerPaid) {
                    fresh.setTrangthai(TrangThaiPhienDauGia.SUCCESS);
                    phiendaugiaRepository.save(fresh);

                    List<Phieuthanhtoantiencoc> allDeposits = phieuthanhtoantiencocRepository.findByPhienDauGia_Maphiendg(fresh.getMaphiendg());
                    //Tien hanh hoan tien coc
                    for (Phieuthanhtoantiencoc p : allDeposits) {
                        if (p.getTrangthai() == TrangThaiPhieuThanhToanTienCoc.REFUNDING) {
                            if (p.getTaiKhoan().getEmail().equals(phieu.getTaiKhoan().getEmail())) {
                                p.setTrangthai(TrangThaiPhieuThanhToanTienCoc.PAID);
                                phieuthanhtoantiencocRepository.save(p);
                                continue;
                            }
                            p.setTrangthai(TrangThaiPhieuThanhToanTienCoc.REFUNDED);
                            phieuthanhtoantiencocRepository.save(p);
                            createNotification(p.getTaiKhoan().getEmail(), "Hoàn tiền cọc", "Tiền cọc của bạn đã được hoàn lại từ phiên đấu giá " + phien.getMaphiendg() + " với sản phẩm là " + phien.getSanPham().getTensp() + " thất bại.");
                        }
                    }
                    //Thong bao cho chu phien
                    try {
                        emailService.sendAuctionEndEmail(fresh.getTaiKhoan(), fresh, "Phiên đấu giá thành công. Người thắng đã thanh toán.");
                        createEndNotification(fresh, "Phiên đấu giá thành công. Người thắng đã thanh toán.", true);
                    } catch (Exception e) {
                        log.error("Loi gui email thanh cong cho phien {}: {}", fresh.getMaphiendg(), e.getMessage());
                    }
                    log.info("Phien {} → SUCCESS (nguoi thang da thanh toan)", fresh.getMaphiendg());
                }
                //Nguoi thang khong thanh toan
                else {
                    List<Phientragia> bids = phientragiaRepository.findByPhienDauGia_Maphiendg(fresh.getMaphiendg());
                    if (!bids.isEmpty()) {
                        //Xac dinh nguoi thang dau tien (tra gia cao nhat)
                        Phientragia highestBidder = bids.stream().filter(b -> b.getSotien().equals(
                                bids.stream().map(Phientragia::getSotien).max(Comparator.naturalOrder()).orElse(BigDecimal.ZERO)
                        )).findFirst().orElse(null);

                        //Kiem tra phien co nguoi thang thu 2 khong
                        if (phieu != null && phieu.getTaiKhoan() != null) {
                            boolean hasTransferredToSecond = !phieu.getTaiKhoan().getMatk().equals(
                                    bids.stream().filter(b -> {
                                        assert highestBidder != null;
                                        return b.getSotien().equals(highestBidder.getSotien());
                                    }).findFirst().orElse(new Phientragia()).getTaiKhoan().getMatk()
                            );

                            //Nguoi thang thu 2 khong thanh toan
                            if (hasTransferredToSecond) {
                                if (phieu.getHanthanhtoan().before(new java.util.Date())) {
                                    phieu.setTrangthai(TrangThaiPhieuThanhToan.CANCELLED);
                                    phieuthanhtoanRepository.save(phieu);
                                    fresh.setTrangthai(TrangThaiPhienDauGia.FAILED);
                                    phiendaugiaRepository.save(fresh);

                                    //Hoan tien coc cho nhung nguoi con lai (tru 1 va 2)
                                    List<Phieuthanhtoantiencoc> allDeposits = phieuthanhtoantiencocRepository.findByPhienDauGia_Maphiendg(fresh.getMaphiendg());
                                    String firstWinnerId = highestBidder.getTaiKhoan().getMatk();
                                    String secondWinnerId = (bids.size() > 1) ? bids.stream().sorted(Comparator.comparing(Phientragia::getSotien).reversed()).skip(1).findFirst().get().getTaiKhoan().getMatk() : null;
                                    if (secondWinnerId != null) {
                                        for (Phieuthanhtoantiencoc p : allDeposits) {
                                            if (p.getTaiKhoan().getMatk().equals(secondWinnerId) && p.getTrangthai() == TrangThaiPhieuThanhToanTienCoc.REFUNDING) {
                                                p.setTrangthai(TrangThaiPhieuThanhToanTienCoc.LOST);
                                                phieuthanhtoantiencocRepository.save(p);
                                                createNotification(p.getTaiKhoan().getEmail(), "Hình phạt không thanh toán phiên", "Tiền cọc của bạn đã mất do không thanh toán phiên " + fresh.getMaphiendg() + ".");
                                            }
                                        }
                                    }
                                    for (Phieuthanhtoantiencoc p : allDeposits) {
                                        if (p.getTrangthai() == TrangThaiPhieuThanhToanTienCoc.REFUNDING && !p.getTaiKhoan().getMatk().equals(firstWinnerId) && (secondWinnerId == null || !p.getTaiKhoan().getMatk().equals(secondWinnerId))) {
                                            p.setTrangthai(TrangThaiPhieuThanhToanTienCoc.REFUNDED);
                                            phieuthanhtoantiencocRepository.save(p);
                                            createNotification(p.getTaiKhoan().getEmail(), "Hoàn tiền cọc", "Tiền cọc của bạn đã được hoàn lại do phiên đấu giá " + fresh.getMaphiendg() + " với sản phẩm là " + fresh.getSanPham().getTensp() + " thất bại.");
                                        }
                                    }
                                    //Thong bao cho chu phien
                                    try {
                                        emailService.sendAuctionEndEmail(fresh.getTaiKhoan(), fresh, "Người thắng thứ hai không thanh toán.");
                                        createEndNotification(fresh, "Người thắng thứ hai không thanh toán.", false);
                                    } catch (Exception e) {
                                        log.error("Loi gui email that bai cho phien {}: {}", fresh.getMaphiendg(), e.getMessage());
                                    }
                                    log.info("Phien {} → FAILED (nguoi thang thu hai khong thanh toan)", fresh.getMaphiendg());
                                    return;
                                } else {
                                    log.debug("Phien {} da chuyen, cho nguoi thu hai thanh toan trong thoi gian gia han.", fresh.getMaphiendg());
                                    return;
                                }
                            }
                        }
                    }
                    //Gia tra cao thu 2
                    BigDecimal secondHighestBid = bids.stream()
                            .map(Phientragia::getSotien)
                            .sorted(Comparator.reverseOrder())
                            .skip(1) //Bo qua luot tra gia cao nhat
                            .findFirst()
                            .orElse(BigDecimal.ZERO);

                    if (bids.size() > 1) {
                        //Thong tin nguoi tra gia cao thu 2
                        Phientragia secondWinnerBid = bids.stream()
                                .sorted(Comparator.comparing(Phientragia::getSotien).reversed())
                                .skip(1) //Lay luot tra gia cao thu 2
                                .findFirst()
                                .orElse(null);

                        if (secondWinnerBid != null) {
                            if (phieu == null) throw new AssertionError();
                            //Thong bao hinh phat cho nguoi thu 1
                            if (phieu.getTaiKhoan() == null) throw new AssertionError();
                            emailService.sendAuctionCancelWinEmail(phieu.getTaiKhoan(), fresh);
                            Phieuthanhtoantiencoc phieuthanhtoantiencoc = phieuthanhtoantiencocRepository.findByTaiKhoan_MatkAndPhienDauGia_Maphiendg(phieu.getTaiKhoan().getMatk(), fresh.getMaphiendg())
                                    .orElseThrow(() -> new NotFoundException("Không tìm thấy phiếu thanh toán"));
                            phieuthanhtoantiencoc.setTrangthai(TrangThaiPhieuThanhToanTienCoc.LOST);
                            phieuthanhtoantiencocRepository.save(phieuthanhtoantiencoc);
                            createNotification(phieu.getTaiKhoan().getEmail(), "Hình phạt không thanh toán phiên", "Tiền cọc của bạn đã mất do không thanh toán phiên " + fresh.getMaphiendg() + ".");
                            //Tao phieu thanh toan cho nguoi thu 2
                            Phieuthanhtoan newPhieu = new Phieuthanhtoan();
                            newPhieu.setPhienDauGia(fresh);
                            newPhieu.setTaiKhoan(secondWinnerBid.getTaiKhoan());
                            newPhieu.setSotien(secondHighestBid.subtract(fresh.getTiencoc()));
                            newPhieu.setHanthanhtoan(new Timestamp(System.currentTimeMillis() + SEVEN_DAYS_MS));
                            newPhieu.setTrangthai(TrangThaiPhieuThanhToan.UNPAID);
                            fresh.getPhieuThanhToan().add(newPhieu);
                            phieuthanhtoanRepository.save(newPhieu);
                            //Huy phieu cua nguoi thu 1, cap nhat gia cao nhat cho phien
                            phieu.setTrangthai(TrangThaiPhieuThanhToan.CANCELLED);
                            phieuthanhtoanRepository.save(phieu);
                            fresh.setGiacaonhatdatduoc(secondHighestBid);
                            phiendaugiaRepository.save(fresh);
                            emailService.sendAuctionTransferEmail(secondWinnerBid.getTaiKhoan(), fresh, secondHighestBid);
                            createNotification(secondWinnerBid.getTaiKhoan().getEmail(), "Bạn được chuyển quyền thắng phiên đấu giá.",
                                    String.format("Phiên đấu giá %s với sản phẩm là '%s' đã chuyển cho bạn với giá %s.", fresh.getMaphiendg(), fresh.getSanPham().getTensp(), formatCurrency(secondHighestBid)));
                            schedulePaymentCheck(fresh);
                            log.info("Phien {}: Chuyen cho nguoi thang thu hai {}", fresh.getMaphiendg(), secondWinnerBid.getTaiKhoan().getEmail());
                        }
                        //Khong co nguoi thang thu 2
                        else {
                            log.warn("Khong tim thay nguoi thang thu hai cho {}", fresh.getMaphiendg());
                            fresh.setTrangthai(TrangThaiPhienDauGia.FAILED);
                            phiendaugiaRepository.save(fresh);
                            //Hoan coc (tru nguoi thu 1)
                            List<Phieuthanhtoantiencoc> allDeposits = phieuthanhtoantiencocRepository.findByPhienDauGia_Maphiendg(fresh.getMaphiendg());
                            String firstWinnerId = bids.stream().max(Comparator.comparing(Phientragia::getSotien)).get().getTaiKhoan().getMatk();
                            String secondWinnerId = null;
                            for (Phieuthanhtoantiencoc p : allDeposits) {
                                if (p.getTrangthai() == TrangThaiPhieuThanhToanTienCoc.REFUNDING && !p.getTaiKhoan().getMatk().equals(firstWinnerId) && (secondWinnerId == null || !p.getTaiKhoan().getMatk().equals(secondWinnerId))) {
                                    p.setTrangthai(TrangThaiPhieuThanhToanTienCoc.REFUNDED);
                                    phieuthanhtoantiencocRepository.save(p);
                                    createNotification(p.getTaiKhoan().getEmail(), "Hoàn tiền cọc", "Tiền cọc của bạn đã được hoàn lại do phiên đấu giá " + fresh.getMaphiendg() + " với sản phẩm là " + fresh.getSanPham().getTensp() + " thất bại.");
                                }
                            }
                            try {
                                emailService.sendAuctionEndEmail(fresh.getTaiKhoan(), fresh, "Nguoi thang khong thanh toan va khong co nguoi ke tiep.");
                                createEndNotification(fresh, "Người thắng không thanh toán và không có người kế tiếp.", false);
                            } catch (Exception ignored) {
                            }
                        }
                    }
                    //Khong co nguoi thang thu 2
                    else {
                        fresh.setTrangthai(TrangThaiPhienDauGia.FAILED);
                        phiendaugiaRepository.save(fresh);
                        //Hoan coc (tru nguoi 1)
                        List<Phieuthanhtoantiencoc> allDeposits = phieuthanhtoantiencocRepository.findByPhienDauGia_Maphiendg(fresh.getMaphiendg());
                        String firstWinnerId = bids.stream().max(Comparator.comparing(Phientragia::getSotien)).get().getTaiKhoan().getMatk();
                        for (Phieuthanhtoantiencoc p : allDeposits) {
                            if (p.getTrangthai() == TrangThaiPhieuThanhToanTienCoc.REFUNDING && !p.getTaiKhoan().getMatk().equals(firstWinnerId)) {
                                p.setTrangthai(TrangThaiPhieuThanhToanTienCoc.REFUNDED);
                                phieuthanhtoantiencocRepository.save(p);
                                createNotification(p.getTaiKhoan().getEmail(), "Hoàn tiền cọc", "Tiền cọc của bạn đã được hoàn lại do phiên đấu giá " + fresh.getMaphiendg() + " với sản phẩm là " + fresh.getSanPham().getTensp() + " thất bại.");
                            }
                        }
                        try {
                            emailService.sendAuctionEndEmail(fresh.getTaiKhoan(), fresh, "Nguoi thang khong thanh toan va khong co nguoi ke tiep du dieu kien.");
                        } catch (Exception ignored) {
                        }
                        log.info("Phien {} → FAILED (nguoi thang khong thanh toan, khong co thay the)", fresh.getMaphiendg());
                    }
                }
            } catch (Exception e) {
                log.error("Ket thuc kiem tra thanh toan that bai {}: {}", phien.getMaphiendg(), e.getMessage());
            } finally {
                finalizationLocks.remove(maphiendg);
            }
        }
    }

    //Huy toan bo lich cua phien
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

    //Huy phien
    public void cancelAuction(String maphiendg, String reason) {
        try {
            Phiendaugia phien = phiendaugiaRepository.findByIdWithPhieuThanhToan(maphiendg).orElse(null);
            if (phien == null) return;

            if (phien.getTrangthai() != TrangThaiPhienDauGia.CANCELLED) {
                phien.setTrangthai(TrangThaiPhienDauGia.CANCELLED);
                phiendaugiaRepository.save(phien);
                cancelScheduledTask(maphiendg);
                try {
                    emailService.sendAuctionEndEmail(phien.getTaiKhoan(), phien, "Phien dau gia da bi huy: " + reason);
                    createEndNotification(phien, "Phiên đấu giá " + phien.getMaphiendg() + " với sản phẩm là " + phien.getSanPham().getTensp() + " đã bị hủy: " + reason, false);
                } catch (Exception e) {
                    log.error("Loi gui email huy cho phien {}: {}", maphiendg, e.getMessage());
                }
                log.info("Phien {} → CANCELLED (ly do: {})", maphiendg, reason);
                preStartNotifiedSessions.remove(maphiendg);
            }
        } catch (Exception e) {
            log.error("Huy phien that bai {}: {}", maphiendg, e.getMessage());
        }
    }


    private void schedulePreStartNotifyOnce(Phiendaugia phien, long now, long startTime) throws MessagingException, IOException {
        //Da thong bao khong xu ly lai
        if (preStartNotifiedSessions.contains(phien.getMaphiendg())) return;
        long diff = startTime - now;
        //phien dang dien ra khong can len lich
        if (diff <= 0) {
            return;
        }
        //Len lich
        if (diff > ONE_DAY_MS) {
            long notifyAt = startTime - ONE_DAY_MS;
//            long delay = notifyAt - now;
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
        //Tgian <= 1 ngay thong bao ngay
        doNotifyPreStart(phien);
    }

    private void doNotifyPreStart(Phiendaugia phien) throws MessagingException, IOException {
        if (preStartNotifiedSessions.contains(phien.getMaphiendg())) return;
        Phiendaugia fresh = phiendaugiaRepository.findByIdWithPhieuThanhToan(phien.getMaphiendg()).orElse(phien);

        if (!(fresh.getTrangthai() == TrangThaiPhienDauGia.NOT_STARTED
                || fresh.getTrangthai() == TrangThaiPhienDauGia.APPROVED)) {
            return;
        }

        List<Phieuthanhtoantiencoc> paid =
                phieuthanhtoantiencocRepository.findByPhienDauGia_MaphiendgAndTrangthai(
                        fresh.getMaphiendg(), TrangThaiPhieuThanhToanTienCoc.PAID);

        log.debug("Gui thong bao bat dau truoc (24h) cho {} -> {} phieu coc da thanh toan",
                fresh.getMaphiendg(), paid.size());
        //Thong bao
        for (Phieuthanhtoantiencoc p : paid) {
            if (p.getTaiKhoan() != null) {
                emailService.sendAuctionBeginEmail(p.getTaiKhoan(), fresh);  // @Async sẽ handle
                createNotification(p.getTaiKhoan().getEmail(), "Thông báo bắt đầu phiên đấu giá",
                        String.format("Phiên %s với sản phẩm là '%s' sẽ bắt đầu trong 24 giờ.", phien.getMaphiendg(), fresh.getSanPham().getTensp()));
            }
        }
        //Huy phieu coc chua thanh toan
        List<Phieuthanhtoantiencoc> unpaid = phieuthanhtoantiencocRepository.findByPhienDauGia_MaphiendgAndTrangthai(
                fresh.getMaphiendg(), TrangThaiPhieuThanhToanTienCoc.UNPAID);
        for (Phieuthanhtoantiencoc p : unpaid) {
            p.setTrangthai(TrangThaiPhieuThanhToanTienCoc.CANCELLED);
            phieuthanhtoantiencocRepository.save(p);
            log.debug("Da huy phieu coc chua thanh toan cho {} trong phien {}", p.getTaiKhoan().getEmail(), fresh.getMaphiendg());
            createNotification(p.getTaiKhoan().getEmail(), "Hủy phiếu cọc", "Phiếu cọc " + p.getMatc() + " của " + phien.getMaphiendg() + " đã bị hủy do không thanh toán trước hạn.");
        }

        preStartNotifiedSessions.add(fresh.getMaphiendg());
    }

    //Duyet phien
    public void scheduleNewOrApprovedAuction(String maphiendg) throws MessagingException, IOException {
        if (maphiendg == null) return;
        Phiendaugia phien = phiendaugiaRepository.findByIdWithPhieuThanhToan(maphiendg).orElse(null);
        if (phien == null || phien.getThoigianbd() == null || phien.getThoigiankt() == null) {
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
                log.warn("Khong the danh dau NOT_STARTED cho {}: {}", maphiendg, e.getMessage());
            }
        }
        scheduleStartOnce(phien);
        scheduleEndOnce(phien);
    }

    private void createNotification(String userEmail, String tieude, String noidung) {
        try {
            ThongBaoCreationRequest request = new ThongBaoCreationRequest();
            request.setTieude(tieude);
            request.setNoidung(noidung);
            String adminEmail = "congtan123@gmail.com";
            thongbaoService.createForUser(request, userEmail);
        } catch (Exception e) {
            log.error("Loi tao thong bao cho {}: {}", userEmail, e.getMessage());
        }
    }

    private void createWinNotification(Phientragia winnerBid, Phiendaugia phien, BigDecimal giaThang) {
        if (winnerBid != null && winnerBid.getTaiKhoan() != null) {
            String tieude = "Chúc mừng! Bạn đã thắng phiên đấu giá";
            String noidung = String.format("Bạn đã thắng phiên %s với sản phẩm là '%s' với giá %s. Vui lòng thanh toán trong thời hạn.", phien.getMaphiendg(), phien.getSanPham().getTensp(), formatCurrency(giaThang));
            createNotification(winnerBid.getTaiKhoan().getEmail(), tieude, noidung);
        }
    }

    private void createEndNotification(Phiendaugia phien, String lydo, boolean isSuccess) {
        if (phien.getTaiKhoan() != null) {
            String tieude = isSuccess ? "Phiên đấu giá thành công" : "Phiên đấu giá thất bại";
            String noidung = String.format("Phiên %s với sản phẩm là '%s' đã kết thúc. Lý do: %s", phien.getMaphiendg(), phien.getSanPham().getTensp(), lydo);
            createNotification(phien.getTaiKhoan().getEmail(), tieude, noidung);
        }
    }

    private String formatCurrency(BigDecimal amount) {
        if (amount == null) return "0 đ";
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setGroupingSeparator('.');
        symbols.setDecimalSeparator(',');
        DecimalFormat formatter = new DecimalFormat("#,###", symbols);
        formatter.setGroupingSize(3);
        formatter.setGroupingUsed(true);
        return formatter.format(amount) + " đ";
    }

    private Phieuthanhtoan getActivePhieu(Phiendaugia phien) {
        return phien.getPhieuThanhToan().stream()
                .filter(p -> p.getTrangthai() != TrangThaiPhieuThanhToan.CANCELLED)
                .max(Comparator.comparing(Phieuthanhtoan::getHanthanhtoan))  // Lấy phiếu mới nhất
                .orElse(null);
    }
}