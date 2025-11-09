package com.example.daugia.service;

import com.example.daugia.dto.response.AuctionDTO;
import com.example.daugia.dto.response.BiddingDTO;
import com.example.daugia.dto.response.UserShortDTO;
import com.example.daugia.entity.Phiendaugia;
import com.example.daugia.entity.Phientragia;
import com.example.daugia.entity.Taikhoan;
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

    public List<BiddingDTO> findAll(){
        List<Phientragia> phientragiaList = phientragiaRepository.findAll();
        return phientragiaList.stream()
                .map(phientragia -> new BiddingDTO(
                        phientragia.getMaphientg(),
                        new UserShortDTO(phientragia.getTaiKhoan().getMatk()),
                        new AuctionDTO(phientragia.getPhienDauGia().getMaphiendg()),
                        phientragia.getSotien(),
                        phientragia.getSolan(),
                        phientragia.getThoigian(),
                        phientragia.getThoigiancho()
                ))
                .toList();
    }

    @Transactional
    public BiddingDTO createBid(String maphienDauGia, String makh, int solan) {
        if (solan < 1) {
            throw new IllegalArgumentException("Số lần trả giá phải lớn hơn hoặc bằng 1");
        }

        // Gợi ý: nếu có thể, dùng repo với PESSIMISTIC_WRITE lock để tránh tranh chấp:
        // Phiendaugia phien = phiendaugiaRepository.findByIdForUpdate(maphienDauGia)
        Phiendaugia phien = phiendaugiaRepository.findById(maphienDauGia)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phiên đấu giá"));
        Taikhoan user = taikhoanRepository.findById(makh)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng"));

        // Kiểm tra thời gian phiên
        Timestamp now = Timestamp.from(Instant.now());
        if (phien.getThoigianbd() != null && now.before(phien.getThoigianbd())) {
            throw new IllegalArgumentException("Phiên chưa bắt đầu, không thể trả giá.");
        }
        if (phien.getThoigiankt() != null && now.after(phien.getThoigiankt())) {
            throw new IllegalArgumentException("Phiên đã kết thúc, không thể trả giá.");
        }

        // Kiểm tra "khóa chờ"
        Optional<Phientragia> lastBid = phientragiaRepository
                .findTopByTaiKhoan_MatkAndPhienDauGia_MaphiendgOrderByThoigianDesc(makh, maphienDauGia);
        if (lastBid.isPresent()) {
            Timestamp lockUntil = lastBid.get().getThoigiancho();
            if (lockUntil != null && lockUntil.after(now)) {
                throw new IllegalArgumentException("Bạn phải đợi hết thời gian chờ mới được trả giá lại!");
            }
        }

        // Tính thời gian chờ mới (20s)
        Timestamp waitUntil = Timestamp.from(now.toInstant().plusSeconds(20));

        // Tính giá mới bằng BigDecimal
        BigDecimal giaKhoiDiem = requireNonNull(phien.getGiakhoidiem(), "Thiếu giá khởi điểm");
        BigDecimal buocGia = requireNonNull(phien.getBuocgia(), "Thiếu bước giá");
        BigDecimal giaCaoNhat = Optional.ofNullable(phien.getGiacaonhatdatduoc()).orElse(BigDecimal.ZERO);

        // tổng tăng = buocGia * solan
        BigDecimal tangThem = buocGia.multiply(BigDecimal.valueOf(solan));

        // "Lần đầu" nếu chưa có giá hoặc giá cao nhất <= giá khởi điểm
        boolean lanDau = (giaCaoNhat.compareTo(giaKhoiDiem) <= 0);

        BigDecimal newPrice = (lanDau ? giaKhoiDiem : giaCaoNhat).add(tangThem);

        // (Tuỳ chọn) chuẩn hoá scale theo bước giá hoặc 0 chữ số thập phân
        newPrice = newPrice.setScale(Math.max(0, buocGia.scale()), RoundingMode.HALF_UP);

        // Cập nhật giá cao nhất
        phien.setGiacaonhatdatduoc(newPrice);
        phiendaugiaRepository.save(phien);

        // Lưu bản ghi trả giá
        Phientragia ptg = new Phientragia();
        ptg.setPhienDauGia(phien);
        ptg.setTaiKhoan(user);
        ptg.setSolan(solan);
        ptg.setSotien(newPrice);
        ptg.setThoigian(now);
        ptg.setThoigiancho(waitUntil);
        phientragiaRepository.save(ptg);

        return new BiddingDTO(
                ptg.getMaphientg(),
                new UserShortDTO(user.getMatk(), user.getHo(), user.getTenlot(), user.getTen(), user.getEmail(), user.getSdt()),
                new AuctionDTO(phien.getMaphiendg()),
                ptg.getSotien(),
                ptg.getSolan(),
                ptg.getThoigian(),
                ptg.getThoigiancho()
        );
    }

    private static <T> T requireNonNull(T val, String msg) {
        if (val == null) throw new IllegalArgumentException(msg);
        return val;
    }
}
