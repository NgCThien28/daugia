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
    @Autowired
    private AuctionSchedulerService auctionSchedulerService;

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

    public List<AuctionDTO> findByStatuses(List<TrangThaiPhienDauGia> statuses) {
        // Lấy tất cả phiên có trạng thái thuộc danh sách
        List<Phiendaugia> phienList = phiendaugiaRepository.findAll()
                .stream()
                .filter(phien -> statuses.contains(phien.getTrangthai()))
                .toList();

        // Map sang AuctionDTO
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
                                phien.getSanPham().getTensp(),
                                phien.getSanPham().getMasp(),
                                phien.getSanPham().getDanhMuc().getMadm(),
                                new CityDTO(
                                        phien.getSanPham().getThanhPho().getMatp(),
                                        phien.getSanPham().getThanhPho().getTentp()
                                ),
                                phien.getSanPham().getHinhAnh().stream()
                                        .map(ha -> new ImageDTO(ha.getMaanh(), ha.getTenanh()))
                                        .toList()
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
//        auctionSchedulerService.scheduleNewOrApprovedAuction(phiendaugia.getMaphiendg());
        return customAuction(phiendaugia);
    }
}
