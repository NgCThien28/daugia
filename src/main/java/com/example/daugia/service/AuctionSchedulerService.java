package com.example.daugia.service;

import com.example.daugia.core.enums.TrangThaiPhienDauGia;
import com.example.daugia.entity.Phiendaugia;
import com.example.daugia.repository.PhiendaugiaRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

@Service
@RequiredArgsConstructor
public class AuctionSchedulerService {

    @Autowired
    private ThreadPoolTaskScheduler scheduler;

    @Autowired
    private PhiendaugiaRepository phiendaugiaRepository;

    // Lưu tất cả phiên đấu giá trong memory
    private final Map<String, Phiendaugia> tempAuctions = new ConcurrentHashMap<>();

    // Lưu các task đang được hẹn
    private final Map<String, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        // Load tất cả phiên từ DB và schedule các mốc thời gian còn lại
        syncAndScheduleAllAuctions();

        // Đồng bộ DB mỗi 30 phút để lấy các phiên mới
        scheduler.scheduleAtFixedRate(this::syncAndScheduleAllAuctions, 30 * 60_000);
    }

    /**
     * Đồng bộ DB vào tempAuctions và schedule các mốc thời gian
     */
    private void syncAndScheduleAllAuctions() {
        List<Phiendaugia> allAuctions = phiendaugiaRepository.findAll();
        long now = System.currentTimeMillis();

        for (Phiendaugia phien : allAuctions) {
            tempAuctions.put(phien.getMaphiendg(), phien);

            long startTime = phien.getThoigianbd().getTime();
            long endTime = phien.getThoigiankt().getTime();

            // APPROVED → NOT_STARTED nếu thời gian chưa tới
            if (phien.getTrangthai() == TrangThaiPhienDauGia.APPROVED && now < startTime) {
                phien.setTrangthai(TrangThaiPhienDauGia.NOT_STARTED);
                phiendaugiaRepository.save(phien);

            }

            // Schedule bắt đầu nếu NOT_STARTED và thời gian còn lại
            if ((phien.getTrangthai() == TrangThaiPhienDauGia.NOT_STARTED || phien.getTrangthai() == TrangThaiPhienDauGia.APPROVED)
                    && now < startTime) {
                scheduleStart(phien);
            }

            // Schedule kết thúc nếu IN_PROGRESS hoặc NOT_STARTED đã tới thời gian kết thúc
            if ((phien.getTrangthai() == TrangThaiPhienDauGia.IN_PROGRESS || phien.getTrangthai() == TrangThaiPhienDauGia.NOT_STARTED)
                    && now < endTime) {
                scheduleEnd(phien);
            }

            // Nếu phiên đã qua thời gian kết thúc nhưng chưa chuyển trạng thái
            if (now >= endTime && phien.getTrangthai() != TrangThaiPhienDauGia.WAITING_FOR_PAYMENT) {
                endAuction(phien);
            }

            // Nếu NOT_STARTED mà thời gian bắt đầu đã tới → bắt đầu ngay
            if (phien.getTrangthai() == TrangThaiPhienDauGia.NOT_STARTED && now >= startTime && now < endTime) {
                startAuction(phien);
                scheduleEnd(phien);
            }
        }
    }

    /**
     * Schedule task bắt đầu phiên
     */
    private void scheduleStart(Phiendaugia phien) {
        long delay = phien.getThoigianbd().getTime() - System.currentTimeMillis();
        if (delay <= 0) return;

        ScheduledFuture<?> future = scheduler.schedule(
                () -> startAuction(phien),
                Instant.ofEpochMilli(System.currentTimeMillis() + delay)
        );
        scheduledTasks.put(phien.getMaphiendg() + "_start", future);
    }

    /**
     * Schedule task kết thúc phiên
     */
    private void scheduleEnd(Phiendaugia phien) {
        long delay = phien.getThoigiankt().getTime() - System.currentTimeMillis();
        if (delay <= 0) {
            endAuction(phien);
            return;
        }

        ScheduledFuture<?> future = scheduler.schedule(
                () -> endAuction(phien),
                Instant.ofEpochMilli(System.currentTimeMillis() + delay)
        );
        scheduledTasks.put(phien.getMaphiendg() + "_end", future);
    }

    /**
     * Bắt đầu phiên đấu giá
     */
    private void startAuction(Phiendaugia phien) {
        phien.setTrangthai(TrangThaiPhienDauGia.IN_PROGRESS);
        phiendaugiaRepository.save(phien);
        tempAuctions.put(phien.getMaphiendg(), phien);
        System.out.println("▶ Phiên " + phien.getMaphiendg() + " bắt đầu → IN_PROGRESS");
    }

    /**
     * Kết thúc phiên đấu giá
     */
    private void endAuction(Phiendaugia phien) {
        phien.setTrangthai(TrangThaiPhienDauGia.WAITING_FOR_PAYMENT);
        phiendaugiaRepository.save(phien);
        tempAuctions.put(phien.getMaphiendg(), phien);
        scheduledTasks.remove(phien.getMaphiendg() + "_start");
        scheduledTasks.remove(phien.getMaphiendg() + "_end");
        System.out.println("✅ Phiên " + phien.getMaphiendg() + " kết thúc → WAITING_FOR_PAYMENT");
    }

    /**
     * Hủy task khi admin hủy phiên
     */
    public void cancelScheduledTask(String maphiendg) {
        ScheduledFuture<?> startTask = scheduledTasks.remove(maphiendg + "_start");
        ScheduledFuture<?> endTask = scheduledTasks.remove(maphiendg + "_end");
        if (startTask != null) startTask.cancel(false);
        if (endTask != null) endTask.cancel(false);
        System.out.println("❌ Đã hủy task của phiên " + maphiendg);
    }

    /**
     * Thêm phiên mới hoặc duyệt phiên → schedule các mốc thời gian
     */
    public void scheduleNewOrApprovedAuction() {
        syncAndScheduleAllAuctions();
    }
}
