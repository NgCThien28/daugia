package com.example.daugia.service;

import com.example.daugia.core.enums.KetQuaPhien;
import com.example.daugia.core.enums.TrangThaiPhienDauGia;
import com.example.daugia.core.enums.TrangThaiSanPham;
import com.example.daugia.dto.request.PhiendaugiaCreationRequest;
import com.example.daugia.dto.response.AuctionDTO;
import com.example.daugia.dto.response.ProductDTO;
import com.example.daugia.dto.response.UserShortDTO;
import com.example.daugia.entity.Phiendaugia;
import com.example.daugia.entity.Sanpham;
import com.example.daugia.entity.Taikhoan;
import com.example.daugia.repository.PhiendaugiaRepository;
import com.example.daugia.repository.SanphamRepository;
import com.example.daugia.repository.TaikhoanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PhiendaugiaService {
    @Autowired
    private PhiendaugiaRepository phiendaugiaRepository;
    @Autowired
    private TaikhoanRepository taikhoanRepository;
    @Autowired
    private SanphamRepository sanphamRepository;

    public List<AuctionDTO> findAll() {
        List<Phiendaugia> phienList = phiendaugiaRepository.findAll();
        return phienList.stream()
                .map(phien -> new AuctionDTO(
                        phien.getMaphiendg(),
                        new UserShortDTO(
                                phien.getTaiKhoan().getMatk(),
                                phien.getTaiKhoan().getHo(),
                                phien.getTaiKhoan().getTenlot(),
                                phien.getTaiKhoan().getTen(),
                                phien.getTaiKhoan().getEmail(),
                                phien.getTaiKhoan().getSdt()
                        ),
                        new ProductDTO(
                                phien.getSanPham().getMasp(),
                                phien.getSanPham().getTensp()
                        ),
                        phien.getTrangthai(),
                        phien.getThoigianbd(),
                        phien.getThoigiankt(),
                        phien.getThoigianbddk(),
                        phien.getThoigianktdk(),
                        phien.getGiakhoidiem(),
                        phien.getGiatran(),
                        phien.getBuocgia(),
                        phien.getTiencoc()
                ))
                .toList();
    }
    public List<Phiendaugia> findByUser(String email){
        Taikhoan taikhoan = taikhoanRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản"));
        List<Phiendaugia> phiendaugiaList = phiendaugiaRepository.findByTaiKhoan_Matk(taikhoan.getMatk());
        return phiendaugiaList;
    }

    public AuctionDTO customAuction(Phiendaugia phiendaugia){
        UserShortDTO userShortDTO = new UserShortDTO(phiendaugia.getTaiKhoan().getMatk());
        ProductDTO productDTO = new ProductDTO(
                phiendaugia.getSanPham().getMasp(),
                phiendaugia.getSanPham().getTensp()
        );
        AuctionDTO auctionDTO = new AuctionDTO(
                phiendaugia.getMaphiendg(),
                userShortDTO,productDTO,
                phiendaugia.getTrangthai(),
                phiendaugia.getThoigianbd(),
                phiendaugia.getThoigiankt(),
                phiendaugia.getThoigianbddk(),
                phiendaugia.getThoigianktdk(),
                phiendaugia.getGiakhoidiem(),
                phiendaugia.getGiatran(),
                phiendaugia.getBuocgia(),
                phiendaugia.getTiencoc()
        );
        return auctionDTO;
    }

    public AuctionDTO create(PhiendaugiaCreationRequest request, String email) {
        Phiendaugia phiendaugia = new Phiendaugia();
        phiendaugia.setTaiKhoan(taikhoanRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản khách hàng")));
        Sanpham sanpham = sanphamRepository.findById(request.getMasp())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy san pham"));
        if(sanpham.getTrangthai().equals(TrangThaiSanPham.APPROVED)){
            phiendaugia.setSanPham(sanpham);
            sanpham.setTrangthai(TrangThaiSanPham.AUCTION_CREATED);
            sanphamRepository.save(sanpham);
        } else {
            throw new IllegalArgumentException("San pham chua duoc duyet");
        }
        phiendaugia.setThoigianbd(request.getThoigianbd());
        phiendaugia.setThoigiankt(request.getThoigiankt());
        phiendaugia.setThoigianbddk(request.getThoigianbddk());
        phiendaugia.setThoigianktdk(request.getThoigianktdk());
        phiendaugia.setGiakhoidiem(request.getGiakhoidiem());
        phiendaugia.setGiatran(request.getGiatran());
        phiendaugia.setBuocgia(request.getBuocgia());
        phiendaugia.setTiencoc(request.getTiencoc());
        phiendaugia.setKetquaphien(KetQuaPhien.PENDING_APPROVAL);
        phiendaugia.setTrangthai(TrangThaiPhienDauGia.PENDING_APPROVAL);
        phiendaugiaRepository.save(phiendaugia);
        AuctionDTO auction = customAuction(phiendaugia);
        return auction;
    }
}
