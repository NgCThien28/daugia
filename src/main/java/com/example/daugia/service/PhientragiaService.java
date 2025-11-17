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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class PhientragiaService {
    @Autowired
    private PhientragiaRepository phientragiaRepository;
    @Autowired
    private PhiendaugiaRepository phiendaugiaRepository;
    @Autowired
    private TaikhoanRepository taikhoanRepository;
    private static final int WAIT_SECONDS = 20;

    public List<BiddingDTO> findAll() {
        return phientragiaRepository.findAll()
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public BiddingDTO createBid(String maphienDauGia, String makh, int solan) {
        if (solan < 1) {
            throw new ValidationException("Số lần trả giá phải ≥ 1");
        }

        Phiendaugia phien = phiendaugiaRepository.findById(maphienDauGia)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy phiên đấu giá"));
        Taikhoan user = taikhoanRepository.findById(makh)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy người dùng"));

        Timestamp now = Timestamp.from(Instant.now());
        validateAuctionTime(phien, now);
        enforceUserCooldown(makh, maphienDauGia, now);

        BigDecimal newPrice = calculateNewPrice(phien, solan);

        // Cập nhật giá cao nhất
        phien.setGiacaonhatdatduoc(newPrice);
        phiendaugiaRepository.save(phien);

        // Thời gian chờ mới
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
        // Scale theo bước giá
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
