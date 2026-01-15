package com.example.daugia.service;

import com.example.daugia.dto.response.AuctionDTO;
import com.example.daugia.dto.response.BiddingDTO;
import com.example.daugia.dto.response.UserShortDTO;
import com.example.daugia.entity.Phiendaugia;
import com.example.daugia.entity.Phientragia;
import com.example.daugia.entity.Phieuthanhtoantiencoc;
import com.example.daugia.entity.Taikhoan;
import com.example.daugia.exception.NotFoundException;
import com.example.daugia.exception.ValidationException;
import com.example.daugia.repository.PhiendaugiaRepository;
import com.example.daugia.repository.PhientragiaRepository;
import com.example.daugia.repository.TaikhoanRepository;
import com.example.daugia.repository.PhieuthanhtoantiencocRepository; // Thêm import
import com.example.daugia.core.enums.TrangThaiPhieuThanhToanTienCoc; // Thêm import nếu cần
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
    @Autowired
    private PhieuthanhtoantiencocRepository phieuthanhtoantiencocRepository; // Thêm injection

    private static final int WAIT_SECONDS = 10;

    // So lan retry khi gap xung dot optimistic lock
    private static final int MAX_RETRY = 3;

    public List<BiddingDTO> findAll() {
        return phientragiaRepository.findAll()
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public BiddingDTO createBid(String maphienDauGia, String makh, int solan) {
        if (solan < 0) {
            throw new ValidationException("Số lần trả giá phải lớn hơn 0");
        }

        int attempt = 0;
        while (true) {
            attempt++;
            try {
                // Doc du lieu moi, tinh gia
                return doCreateBidOnce(maphienDauGia, makh, solan);
            } catch (ObjectOptimisticLockingFailureException | OptimisticLockException ex) {
                if (attempt >= MAX_RETRY) {
                    throw new ValidationException("Có nhiều người trả giá cùng lúc. Vui lòng thử lại!");
                }
                // Backoff ngau nhien tranh xung dot
                try {
                    Thread.sleep(ThreadLocalRandom.current().nextInt(10, 31));
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                    throw new ValidationException("Hệ thống đang bận. Vui lòng thử lại!");
                }
            }
        }
    }

    private BiddingDTO doCreateBidOnce(String maphienDauGia, String makh, int solan) {
        Phiendaugia phien = phiendaugiaRepository.findById(maphienDauGia)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy phiên đấu giá"));
        Taikhoan user = taikhoanRepository.findById(makh)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy người dùng"));

        // Kiem tra phieu thanh toan ton tai
        if (!hasPaidDeposit(makh, maphienDauGia)) {
            throw new ValidationException("Bạn không thể trả giá cho phiên này!");
        }

        Timestamp now = Timestamp.from(Instant.now());
        validateAuctionTime(phien, now);
        enforceUserCooldown(makh, maphienDauGia, now);
        enforceNoOwnerBidding(phien, makh);
        enforceNoSelfBidding(maphienDauGia,makh);
        BigDecimal newPrice = calculateNewPrice(phien, solan);

        // Cap nhat gia cao nhat
        phien.setGiacaonhatdatduoc(newPrice);
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

    // Helper
    private boolean hasPaidDeposit(String makh, String maphienDauGia) {
        Optional<Phieuthanhtoantiencoc> phieu = phieuthanhtoantiencocRepository
                .findByTaiKhoan_MatkAndPhienDauGia_Maphiendg(makh, maphienDauGia);
        return phieu.isPresent() && TrangThaiPhieuThanhToanTienCoc.PAID.equals(phieu.get().getTrangthai());
    }

    private void validateAuctionTime(Phiendaugia phien, Timestamp now) {
        if (phien.getThoigianbd() != null && now.before(phien.getThoigianbd())) {
            throw new ValidationException("Phiên chưa bắt đầu, không thể trả giá");
        }
        if (phien.getThoigiankt() != null && now.after(phien.getThoigiankt())) {
            throw new ValidationException("Phiên đã kết thúc, không thể trả giá");
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

    private void enforceNoSelfBidding(String maphienDauGia, String makh) {
        Optional<Phientragia> highestBid = phientragiaRepository
                .findTopByPhienDauGia_MaphiendgOrderBySotienDesc(maphienDauGia);
        if (highestBid.isPresent() && highestBid.get().getTaiKhoan().getMatk().equals(makh)) {
            throw new ValidationException("Bạn đang là người trả giá cao nhất, không thể trả giá thêm!");
        }
    }

    private void enforceNoOwnerBidding(Phiendaugia phien, String makh) {
        Taikhoan owner = phien.getTaiKhoan();
        if (owner.getMatk().equals(makh)) {
            throw new ValidationException("Bạn là chủ phiên đấu giá, không được tự trả giá!");
        }
    }

    private BigDecimal calculateNewPrice(Phiendaugia phien, int solan) {
        BigDecimal giaKhoiDiem = nonNull(phien.getGiakhoidiem(), "Thiếu giá khởi điểm");
        BigDecimal buocGia = nonNull(phien.getBuocgia(), "Thiếu bước giá");
        BigDecimal giaCaoNhat = Optional.ofNullable(phien.getGiacaonhatdatduoc()).orElse(BigDecimal.ZERO);

        boolean firstTime = giaCaoNhat.compareTo(giaKhoiDiem) <= 0;

        if (firstTime) {
            // Lan dau: solan = 0 bang gia khoi diem, solan > 0 thi tang solan buoc
            if (solan == 0) {
                return giaKhoiDiem;
            } else if (solan > 0) {
                BigDecimal increase = buocGia.multiply(BigDecimal.valueOf(solan));
                BigDecimal newPrice = giaKhoiDiem.add(increase);
                return newPrice.setScale(Math.max(0, buocGia.scale()), RoundingMode.HALF_UP);
            } else {
                throw new ValidationException("Số lần trả giá không hợp lệ cho lần đầu");
            }
        } else {
            // Cac lan sau: solan >= 1
            if (solan < 1) {
                throw new ValidationException("Số lần bước giá lớn hơn 1");
            }
            BigDecimal increase = buocGia.multiply(BigDecimal.valueOf(solan));
            BigDecimal newPrice = giaCaoNhat.add(increase);
            return newPrice.setScale(Math.max(0, buocGia.scale()), RoundingMode.HALF_UP);
        }
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