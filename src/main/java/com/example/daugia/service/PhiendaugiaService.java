package com.example.daugia.service;

import com.example.daugia.core.enums.TrangThaiPhienDauGia;
import com.example.daugia.core.enums.TrangThaiSanPham;
import com.example.daugia.dto.request.PhiendaugiaCreationRequest;
import com.example.daugia.dto.response.*;
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
                .map(phiendaugia -> new AuctionDTO(
                        phiendaugia.getMaphiendg(),
                        new UserShortDTO(
                                phiendaugia.getTaiKhoan().getMatk(),
                                phiendaugia.getTaiKhoan().getHo(),
                                phiendaugia.getTaiKhoan().getTenlot(),
                                phiendaugia.getTaiKhoan().getTen(),
                                phiendaugia.getTaiKhoan().getEmail(),
                                phiendaugia.getTaiKhoan().getSdt()
                        ),
                        new ProductDTO(
                                phiendaugia.getSanPham().getTensp(),
                                phiendaugia.getSanPham().getMasp(),
                                phiendaugia.getSanPham().getDanhMuc().getMadm(),
                                new CityDTO(phiendaugia.getSanPham().getThanhPho().getTentp()),
                                phiendaugia.getSanPham().getHinhAnh().stream()
                                        .map(ha -> new ImageDTO(ha.getMaanh(), ha.getTenanh()))
                                        .toList()
                        ),
                        phiendaugia.getTrangthai(),
                        phiendaugia.getThoigianbd(),
                        phiendaugia.getThoigiankt(),
                        phiendaugia.getThoigianbddk(),
                        phiendaugia.getThoigianktdk(),
                        phiendaugia.getGiakhoidiem(),
                        phiendaugia.getGiatran(),
                        phiendaugia.getBuocgia(),
                        phiendaugia.getTiencoc()
                ))
                .toList();
    }
    public List<Phiendaugia> findByUser(String email){
        Taikhoan taikhoan = taikhoanRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản"));
        return phiendaugiaRepository.findByTaiKhoan_Matk(taikhoan.getMatk());
    }
    public AuctionDTO findById(String id) {
        Phiendaugia phiendaugia = phiendaugiaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phien dau gia"));
        return new AuctionDTO(
                phiendaugia.getMaphiendg(),
                new UserShortDTO(
                        phiendaugia.getTaiKhoan().getMatk(),
                        phiendaugia.getTaiKhoan().getHo(),
                        phiendaugia.getTaiKhoan().getTenlot(),
                        phiendaugia.getTaiKhoan().getTen(),
                        phiendaugia.getTaiKhoan().getEmail(),
                        phiendaugia.getTaiKhoan().getSdt(),
                        phiendaugia.getTaiKhoan().getDiachi()
                ),
                new ProductDTO(
                        phiendaugia.getSanPham().getTensp(),
                        phiendaugia.getSanPham().getMasp(),
                        phiendaugia.getSanPham().getDanhMuc().getMadm(),
                        new CityDTO(phiendaugia.getSanPham().getThanhPho().getTentp()),
                        phiendaugia.getSanPham().getHinhAnh().stream()
                                .map(ha -> new ImageDTO(ha.getMaanh(), ha.getTenanh()))
                                .toList()
                ),
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
    }

    public List<AuctionDTO> findByStatus(TrangThaiPhienDauGia status) {
        // Lọc danh sách các phiên theo trạng thái
        List<Phiendaugia> phienList = phiendaugiaRepository.findByTrangthai(status);

        // Nếu trạng thái cần tìm là APPROVED, chỉ trả về các phiên có trạng thái đó
        if (status == TrangThaiPhienDauGia.APPROVED) {
            phienList = phienList.stream()
                    .filter(phiendaugia -> phiendaugia.getTrangthai() == TrangThaiPhienDauGia.APPROVED)
                    .toList();
        }

        // Map sang AuctionDTO (giống findAll)
        return phienList.stream()
                .map(phiendaugia -> new AuctionDTO(
                        phiendaugia.getMaphiendg(),
                        new UserShortDTO(
                                phiendaugia.getTaiKhoan().getMatk(),
                                phiendaugia.getTaiKhoan().getHo(),
                                phiendaugia.getTaiKhoan().getTenlot(),
                                phiendaugia.getTaiKhoan().getTen(),
                                phiendaugia.getTaiKhoan().getEmail(),
                                phiendaugia.getTaiKhoan().getSdt()
                        ),
                        new ProductDTO(
                                phiendaugia.getSanPham().getTensp(),
                                phiendaugia.getSanPham().getMasp(),
                                phiendaugia.getSanPham().getDanhMuc().getMadm(),
                                new CityDTO(phiendaugia.getSanPham().getThanhPho().getMatp(),
                                        phiendaugia.getSanPham().getThanhPho().getTentp()),
                                phiendaugia.getSanPham().getHinhAnh().stream()
                                        .map(ha -> new ImageDTO(ha.getMaanh(), ha.getTenanh()))
                                        .toList()
                        ),
                        phiendaugia.getTrangthai(),
                        phiendaugia.getThoigianbd(),
                        phiendaugia.getThoigiankt(),
                        phiendaugia.getThoigianbddk(),
                        phiendaugia.getThoigianktdk(),
                        phiendaugia.getGiakhoidiem(),
                        phiendaugia.getGiatran(),
                        phiendaugia.getBuocgia(),
                        phiendaugia.getTiencoc()
                ))
                .toList();
    }


    public AuctionDTO customAuction(Phiendaugia phiendaugia){
        UserShortDTO userShortDTO = new UserShortDTO(phiendaugia.getTaiKhoan().getMatk());
        ProductDTO productDTO = new ProductDTO(
                phiendaugia.getSanPham().getMasp(),
                phiendaugia.getSanPham().getTensp()
        );
        return new AuctionDTO(
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
        phiendaugia.setTrangthai(TrangThaiPhienDauGia.PENDING_APPROVAL);
        phiendaugiaRepository.save(phiendaugia);
        return customAuction(phiendaugia);
    }
}
