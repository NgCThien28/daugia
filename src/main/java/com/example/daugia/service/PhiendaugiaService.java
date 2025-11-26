package com.example.daugia.service;

import com.example.daugia.core.enums.TrangThaiPhienDauGia;
import com.example.daugia.core.enums.TrangThaiSanPham;
import com.example.daugia.dto.request.PhiendaugiaCreationRequest;
import com.example.daugia.dto.response.*;
import com.example.daugia.entity.Phiendaugia;
import com.example.daugia.entity.Sanpham;
import com.example.daugia.entity.Taikhoan;
import com.example.daugia.exception.ConflictException;
import com.example.daugia.exception.NotFoundException;
import com.example.daugia.exception.ValidationException;
import com.example.daugia.repository.PhiendaugiaRepository;
import com.example.daugia.repository.SanphamRepository;
import com.example.daugia.repository.TaikhoanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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

    public List<AuctionDTO> findAllDTO() {
        return phiendaugiaRepository.findAll()
                .stream()
                .map(this::toAuctionDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<AuctionDTO> getPaidAuctionsByMatk(String email, Pageable pageable) {
        Page<Phiendaugia> page = phiendaugiaRepository.findAuctionsPaidByEmail(email, pageable);
        return page.map(this::toAuctionDTO);
    }

    @Transactional(readOnly = true)
    public List<AuctionDTO> getPaidAuctionsByMatk(String email) {
        List<Phiendaugia> page = phiendaugiaRepository.findAuctionsPaidByEmail(email);
        return page.stream().map(this::toAuctionDTO).toList();
    }

    public AuctionDTO findByIdDTO(String id) {
        Phiendaugia entity = phiendaugiaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy phiên đấu giá"));
        return toAuctionDTO(entity);
    }

    public List<AuctionDTO> findByUser(String email) {
        Taikhoan tk = taikhoanRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy tài khoản người dùng"));
        return phiendaugiaRepository.findByTaiKhoan_Matk(tk.getMatk())
                .stream()
                .map(this::toAuctionDTO)
                .toList();
    }

    public Page<AuctionDTO> findByStatusesPaged(List<TrangThaiPhienDauGia> statuses, Pageable pageable) {
        Page<Phiendaugia> page = phiendaugiaRepository.findByTrangthaiIn(statuses, pageable);
        return page.map(this::toAuctionDTO);
    }

    public Page<AuctionDTO> findFilteredAuctions(
            List<TrangThaiPhienDauGia> statuses,
            String keyword,
            String cateId,
            String regionId,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Long startDateFrom,
            Long startDateTo,
            Pageable pageable
    ) {
        Page<Phiendaugia> page = phiendaugiaRepository.findFilteredAuctions(
                statuses, keyword, cateId, regionId, minPrice, maxPrice, startDateFrom, startDateTo, pageable
        );
        return page.map(this::toAuctionDTO);
    }

    public Page<AuctionDTO> findByStatusPagedWithTimeFilter(TrangThaiPhienDauGia status, Long startDateFrom, Long startDateTo, Pageable pageable) {
        Page<Phiendaugia> page = phiendaugiaRepository.findByTrangthaiAndThoigianbdBetween(status, startDateFrom, startDateTo, pageable);
        return page.map(this::toAuctionDTO);
    }

    public AuctionDTO create(PhiendaugiaCreationRequest request, String email) {
        Taikhoan tk = taikhoanRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy tài khoản khách hàng"));

        boolean existsActive = phiendaugiaRepository.existsBySanPham_Masp(request.getMasp());
        if (existsActive) {
            throw new ConflictException("Sản phẩm đã có phiên đấu giá đang chờ hoặc đang diễn ra");
        }

        Sanpham sp = sanphamRepository.findById(request.getMasp())
                .orElseThrow(() -> new NotFoundException("Không tìm thấy sản phẩm"));

        if (sp.getTrangthai() != TrangThaiSanPham.APPROVED) {
            throw new ValidationException("Sản phẩm chưa được duyệt");
        }

        Phiendaugia pdg = new Phiendaugia();
        pdg.setTaiKhoan(tk);
        pdg.setSanPham(sp);

        sp.setTrangthai(TrangThaiSanPham.AUCTION_CREATED);
        sanphamRepository.save(sp);

        pdg.setThoigianbd(request.getThoigianbd());
        pdg.setThoigiankt(request.getThoigiankt());
        pdg.setThoigianbddk(request.getThoigianbddk());
        pdg.setThoigianktdk(request.getThoigianktdk());
        pdg.setGiakhoidiem(request.getGiakhoidiem());
        pdg.setBuocgia(request.getBuocgia());
        pdg.setTiencoc(request.getTiencoc());
        pdg.setGiacaonhatdatduoc(BigDecimal.ZERO);
        pdg.setTrangthai(TrangThaiPhienDauGia.PENDING_APPROVAL);

        phiendaugiaRepository.save(pdg);
        // auctionSchedulerService.scheduleNewOrApprovedAuction(pdg.getMaphiendg());
        return toAuctionDTO(pdg);
    }

    //  PRIVATE HELPERS
    private void validateAuctionTimes(LocalDateTime start, LocalDateTime end,
                                      LocalDateTime regStart, LocalDateTime regEnd) {

        if (start != null && end != null && !end.isAfter(start)) {
            throw new ValidationException("Thời gian kết thúc phiên phải sau thời gian bắt đầu");
        }
        if (regStart != null && start != null && !regStart.isBefore(start)) {
            throw new ValidationException("Thời gian bắt đầu đăng ký phải trước thời gian bắt đầu phiên");
        }
        if (regEnd != null && regStart != null && !regEnd.isAfter(regStart)) {
            throw new ValidationException("Thời gian kết thúc đăng ký phải sau thời gian bắt đầu đăng ký");
        }
        if (regEnd != null && start != null && !regEnd.isBefore(start)) {
            throw new ValidationException("Thời gian kết thúc đăng ký phải trước thời gian bắt đầu phiên");
        }
    }

    private AuctionDTO toAuctionDTO(Phiendaugia phien) {
        return new AuctionDTO(
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
                phien.getBuocgia(),
                phien.getGiacaonhatdatduoc(),
                phien.getTiencoc(),
                phien.getSlnguoithamgia()
        );
    }
}
