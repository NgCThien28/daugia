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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    public BiddingDTO createBid(String maphienDauGia, String makh, int solan) {
        if (solan < 1)
            throw new IllegalArgumentException("Số lần trả giá phải lớn hơn hoặc bằng 1");

        Phiendaugia phien = phiendaugiaRepository.findById(maphienDauGia)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phiên đấu giá"));
        Taikhoan user = taikhoanRepository.findById(makh)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng"));

        // Kiểm tra xem user có đang bị "khóa chờ" không
        Optional<Phientragia> lastBid = phientragiaRepository.findTopByTaiKhoan_MatkAndPhienDauGia_MaphiendgOrderByThoigianDesc(makh, maphienDauGia);
        if (lastBid.isPresent()) {
            Timestamp now = Timestamp.from(Instant.now());
            if (lastBid.get().getThoigiancho().after(now)) {
                throw new IllegalArgumentException("Bạn phải đợi hết thời gian chờ mới được trả giá lại!");
            }
        }

        Timestamp now = Timestamp.from(Instant.now());
        Timestamp waitUntil = Timestamp.from(now.toInstant().plusSeconds(20));

        double giacaonhat = phien.getGiacaonhatdatduoc();
        double newPrice;

        if (giacaonhat <= phien.getGiakhoidiem()) {
            // lần đầu tiên
            newPrice = phien.getGiakhoidiem() + (solan * phien.getBuocgia());
        } else {
            // lần sau
            newPrice = giacaonhat + (solan * phien.getBuocgia());
        }

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
}
