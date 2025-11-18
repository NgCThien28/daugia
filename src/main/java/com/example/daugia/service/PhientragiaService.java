package com.example.daugia.service;

import com.example.daugia.dto.response.AuctionDTO;
import com.example.daugia.dto.response.BiddingDTO;
import com.example.daugia.dto.response.UserShortDTO;
import com.example.daugia.entity.Phiendaugia;
import com.example.daugia.entity.Phientragia;
import com.example.daugia.entity.Taikhoan;
import com.example.daugia.exception.NotFoundException;
import com.example.daugia.exception.ValidationException;
import com.example.daugia.repository.PhiendaugiaRepository;
import com.example.daugia.repository.PhientragiaRepository;
import com.example.daugia.repository.TaikhoanRepository;
import jakarta.transaction.Transactional;
import jakarta.persistence.OptimisticLockException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class PhientragiaService {
    @Autowired
    private PhientragiaRepository phientragiaRepository;
    @Autowired
    private PhiendaugiaRepository phiendaugiaRepository;
    @Autowired
    private TaikhoanRepository taikhoanRepository;

    private static final int WAIT_SECONDS = 20;

    // số lần retry khi gặp xung đột optimistic lock
    private static final int MAX_RETRY = 3;

    public List<BiddingDTO> findAll() {
        return phientragiaRepository.findAll()
                .stream()
                .map(this::toDto)
                .toList();
    }

    /**
     * Tối ưu cho tải cao:
     * - Dùng Optimistic Lock với @Version ở entity Phiendaugia
     * - Retry tối đa MAX_RETRY lần khi gặp xung đột cập nhật
     * - Backoff ngẫu nhiên 10~30ms để giảm va chạm
     */
    @Transactional
    public BiddingDTO createBid(String maphienDauGia, String makh, int solan) {
        if (solan < 1) {
            throw new ValidationException("Số lần trả giá phải ≥ 1");
        }

        int attempt = 0;
        while (true) {
            attempt++;
            try {
                // Đọc dữ liệu mới nhất, tính giá, update phien với saveAndFlush để kích hoạt kiểm tra version
                return doCreateBidOnce(maphienDauGia, makh, solan);
            } catch (ObjectOptimisticLockingFailureException | OptimisticLockException ex) {
                if (attempt >= MAX_RETRY) {
                    throw new ValidationException("Có nhiều người trả giá cùng lúc. Vui lòng thử lại!");
                }
                // Backoff ngẫu nhiên ngắn để giảm xung đột
                try {
                    Thread.sleep(ThreadLocalRandom.current().nextInt(10, 31));
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                    throw new ValidationException("Hệ thống đang bận. Vui lòng thử lại!");
                }
            }
        }
    }

    /**
     * Thực hiện 1 lần tạo bid:
     * - Validate thời gian, cooldown
     * - Tính giá mới dựa trên giá cao nhất hiện tại
     * - Cập nhật phien (saveAndFlush) -> nếu version thay đổi bởi luồng khác, ném OptimisticLock
     * - Ghi bản ghi trả giá
     */
    private BiddingDTO doCreateBidOnce(String maphienDauGia, String makh, int solan) {
        Phiendaugia phien = phiendaugiaRepository.findById(maphienDauGia)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy phiên đấu giá"));
        Taikhoan user = taikhoanRepository.findById(makh)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy người dùng"));

        Timestamp now = Timestamp.from(Instant.now());
        validateAuctionTime(phien, now);
        enforceUserCooldown(makh, maphienDauGia, now);

        BigDecimal newPrice = calculateNewPrice(phien, solan);

        // Cập nhật giá cao nhất (sẽ kiểm tra @Version khi flush)
        phien.setGiacaonhatdatduoc(newPrice);
        // saveAndFlush để đảm bảo version check xảy ra ngay trong giao dịch này
        phiendaugiaRepository.saveAndFlush(phien);

        Timestamp waitUntil = Timestamp.from(now.toInstant().plusSeconds(WAIT_SECONDS));

        Phientragia ptg = new Phientragia();
        ptg.setPhienDauGia(phien);
        ptg.setTaiKhoan(user);
        ptg.setSolan(solan);
        ptg.setSotien(newPrice);
        ptg.setThoigian(now);
        ptg.setThoigiancho(waitUntil);
        phientragiaRepository.save(ptg);

        return toDto(ptg);
    }

    //    Helper
    private void validateAuctionTime(Phiendaugia phien, Timestamp now) {
        if (phien.getThoigianbd() != null && now.before(phien.getThoigianbd())) {
            throw new ValidationException("Phiên chưa bắt đầu, không thể trả giá.");
        }
        if (phien.getThoigiankt() != null && now.after(phien.getThoigiankt())) {
            throw new ValidationException("Phiên đã kết thúc, không thể trả giá.");
        }
    }

    private void enforceUserCooldown(String makh, String maphienDauGia, Timestamp now) {
        Optional<Phientragia> lastBid = phientragiaRepository
                .findTopByTaiKhoan_MatkAndPhienDauGia_MaphiendgOrderByThoigianDesc(makh, maphienDauGia);
        if (lastBid.isPresent()) {
            Timestamp lockUntil = lastBid.get().getThoigiancho();
            if (lockUntil != null && lockUntil.after(now)) {
                throw new ValidationException("Bạn phải đợi hết thời gian chờ mới được trả giá lại!");
            }
        }
    }

    private BigDecimal calculateNewPrice(Phiendaugia phien, int solan) {
        BigDecimal giaKhoiDiem = nonNull(phien.getGiakhoidiem(), "Thiếu giá khởi điểm");
        BigDecimal buocGia = nonNull(phien.getBuocgia(), "Thiếu bước giá");
        BigDecimal giaCaoNhat = Optional.ofNullable(phien.getGiacaonhatdatduoc()).orElse(BigDecimal.ZERO);

        boolean firstTime = giaCaoNhat.compareTo(giaKhoiDiem) <= 0;
        BigDecimal base = firstTime ? giaKhoiDiem : giaCaoNhat;
        BigDecimal increase = buocGia.multiply(BigDecimal.valueOf(solan));

        BigDecimal newPrice = base.add(increase);
        newPrice = newPrice.setScale(Math.max(0, buocGia.scale()), RoundingMode.HALF_UP);
        return newPrice;
    }

    private <T> T nonNull(T val, String msg) {
        if (val == null) throw new ValidationException(msg);
        return val;
    }

    private BiddingDTO toDto(Phientragia entity) {
        return new BiddingDTO(
                entity.getMaphientg(),
                new UserShortDTO(
                        entity.getTaiKhoan().getMatk(),
                        entity.getTaiKhoan().getHo(),
                        entity.getTaiKhoan().getTenlot(),
                        entity.getTaiKhoan().getTen(),
                        entity.getTaiKhoan().getEmail(),
                        entity.getTaiKhoan().getSdt()
                ),
                new AuctionDTO(entity.getPhienDauGia().getMaphiendg()),
                entity.getSotien(),
                entity.getSolan(),
                entity.getThoigian(),
                entity.getThoigiancho()
        );
    }
}