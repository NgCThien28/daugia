package com.example.daugia.service;

import com.example.daugia.core.enums.TrangThaiPhienDauGia;
import com.example.daugia.core.enums.TrangThaiSanPham;
import com.example.daugia.dto.request.PhiendaugiaCreationRequest;
import com.example.daugia.dto.response.*;
import com.example.daugia.entity.Phiendaugia;
import com.example.daugia.entity.Sanpham;
import com.example.daugia.entity.Taikhoan;
import com.example.daugia.entity.Taikhoanquantri;
import com.example.daugia.exception.ConflictException;
import com.example.daugia.exception.NotFoundException;
import com.example.daugia.exception.ValidationException;
import com.example.daugia.repository.PhiendaugiaRepository;
import com.example.daugia.repository.SanphamRepository;
import com.example.daugia.repository.TaikhoanRepository;
import com.example.daugia.repository.TaikhoanquantriRepository;
import jakarta.mail.MessagingException;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static com.example.daugia.core.enums.TrangThaiPhienDauGia.NOT_REQUESTED;
import static com.example.daugia.core.enums.TrangThaiPhienDauGia.PENDING_APPROVAL;

@Service
public class PhiendaugiaService {
    @Autowired
    private PhiendaugiaRepository phiendaugiaRepository;
    @Autowired
    private TaikhoanRepository taikhoanRepository;
    @Autowired
    private SanphamRepository sanphamRepository;
    @Autowired
    private TaikhoanquantriRepository taikhoanquantriRepository;
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

    public Page<AuctionDTO> findByUser(String email, String keyword, Pageable pageable) {
        Taikhoan tk = taikhoanRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy tài khoản người dùng"));

        Specification<Phiendaugia> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.equal(root.get("taiKhoan").get("matk"), tk.getMatk()));

            if (keyword != null && !keyword.trim().isEmpty()) {
                String keywordLower = "%" + keyword.toLowerCase() + "%";
                Predicate maphiendgPredicate = cb.like(cb.lower(root.get("maphiendg")), keywordLower);
                predicates.add(maphiendgPredicate);
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<Phiendaugia> page = phiendaugiaRepository.findAll(spec, pageable);
        return page.map(this::toAuctionDTO);
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
        if(!email.equals(sp.getTaiKhoan().getEmail()))
            throw new ValidationException("Bạn không phải chủ sản phẩm");
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
        pdg.setTrangthai(NOT_REQUESTED);

        phiendaugiaRepository.save(pdg);
        return toAuctionDTO(pdg);
    }

    public AuctionDTO register(String maphiendg, String email){
        Taikhoan tk = taikhoanRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy tài khoản"));
        Phiendaugia pdg = phiendaugiaRepository.findById(maphiendg)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy phiên đấu giá"));
        if (!pdg.getTaiKhoan().getMatk().equals(tk.getMatk())) {
            throw new ValidationException("Bạn không phải chủ phiên này");
        }
        pdg.setTrangthai(PENDING_APPROVAL);
        phiendaugiaRepository.save(pdg);
        return toAuctionDTO(pdg);
    }

    @Transactional
    public AuctionDTO update(String maphiendg, PhiendaugiaCreationRequest request, String email) {
        Taikhoan tk = taikhoanRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy tài khoản"));

        Phiendaugia pdg = phiendaugiaRepository.findById(maphiendg)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy phiên đấu giá"));

        if (!pdg.getTaiKhoan().getMatk().equals(tk.getMatk())) {
            throw new ValidationException("Bạn không phải chủ phiên này");
        }

        if (pdg.getTrangthai() != PENDING_APPROVAL && pdg.getTrangthai() != NOT_REQUESTED) {
            throw new ValidationException("Chỉ có thể chỉnh sửa phiên đấu giá đang chờ duyệt");
        }

        // Update fields
        pdg.setBuocgia(request.getBuocgia());
        pdg.setTiencoc(request.getTiencoc());
        pdg.setThoigianbd(request.getThoigianbd());
        pdg.setThoigiankt(request.getThoigiankt());
        pdg.setThoigianbddk(request.getThoigianbddk());
        pdg.setThoigianktdk(request.getThoigianktdk());

        Phiendaugia saved = phiendaugiaRepository.save(pdg);
        return toAuctionDTO(saved);
    }

    @Transactional
    public String delete(String maphiendg, String email) {
        // Kiểm tra tồn tại và quyền sở hữu (tùy chọn, nhưng giữ để validation rõ ràng)
        Phiendaugia phiendaugia = phiendaugiaRepository.findById(maphiendg)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy phiên này"));
        if (!email.equals(phiendaugia.getTaiKhoan().getEmail()))
            throw new ValidationException("Bạn không phải chủ phiên này");
        if (phiendaugia.getTrangthai() != PENDING_APPROVAL)
            throw new ValidationException("Phiên đã được duyệt");

        int deleted = phiendaugiaRepository.deletePendingByIdAndOwner(maphiendg, email, PENDING_APPROVAL);
        if (deleted == 0) {
            throw new ValidationException("Không thể xóa phiên này (có thể đã bị thay đổi)");
        }
        return "Xóa phiên thành công!";
    }

//    //  PRIVATE HELPERS
//    private void validateAuctionTimes(Timestamp start, Timestamp end,
//                                      Timestamp regStart, Timestamp regEnd) {
//
//        if (start != null && end != null && !end.after(start)) {
//            throw new ValidationException("Thời gian kết thúc phiên phải sau thời gian bắt đầu");
//        }
//        if (regStart != null && start != null && !regStart.before(start)) {
//            throw new ValidationException("Thời gian bắt đầu đăng ký phải trước thời gian bắt đầu phiên");
//        }
//        if (regEnd != null && regStart != null && !regEnd.after(regStart)) {
//            throw new ValidationException("Thời gian kết thúc đăng ký phải sau thời gian bắt đầu đăng ký");
//        }
//        if (regEnd != null && start != null && !regEnd.before(start)) {
//            throw new ValidationException("Thời gian kết thúc đăng ký phải trước thời gian bắt đầu phiên");
//        }
//    }

    //Admin
    public AuctionDTO approveAuction(PhiendaugiaCreationRequest request, String mapdg, String email) throws MessagingException, IOException {
        Phiendaugia phiendaugia = phiendaugiaRepository.findById(mapdg)
                .orElseThrow(() ->new NotFoundException("Không tìm thấy phiên đấu giá"));
        if (phiendaugia.getTrangthai() != TrangThaiPhienDauGia.PENDING_APPROVAL)
            throw new ValidationException("Phiên đấu giá đã được duyệt");
        Taikhoanquantri admin = taikhoanquantriRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy tài khoản quản trị"));

        phiendaugia.setThoigianbddk(request.getThoigianbddk());
        phiendaugia.setThoigianktdk(request.getThoigianktdk());
        phiendaugia.setThoigianbd(request.getThoigianbd());
        phiendaugia.setThoigiankt(request.getThoigiankt());
        phiendaugia.setGiakhoidiem(request.getGiakhoidiem());
        phiendaugia.setBuocgia(request.getBuocgia());
        phiendaugia.setTiencoc(request.getTiencoc());
        phiendaugia.setTrangthai(TrangThaiPhienDauGia.APPROVED);
        phiendaugia.setTaiKhoanQuanTri(admin);

        Phiendaugia saved = phiendaugiaRepository.save(phiendaugia);
        auctionSchedulerService.scheduleNewOrApprovedAuction(phiendaugia.getMaphiendg());
        return toAuctionDTO(saved);
    }

    //Admin
    public AuctionDTO rejectAuction(String mapdg, String email) {
        Phiendaugia phiendaugia = phiendaugiaRepository.findById(mapdg)
                .orElseThrow(() ->new NotFoundException("Không tìm thấy phiên đấu giá"));
        if (phiendaugia.getTrangthai() != TrangThaiPhienDauGia.PENDING_APPROVAL)
            throw new ValidationException("Phiên đấu giá đã được duyệt");
        Taikhoanquantri admin = taikhoanquantriRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy tài khoản quản trị"));

        phiendaugia.setTrangthai(TrangThaiPhienDauGia.CANCELLED);
        phiendaugia.setTaiKhoanQuanTri(admin);

        Phiendaugia saved = phiendaugiaRepository.save(phiendaugia);
        return toAuctionDTO(saved);
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
                        phien.getTaiKhoan().getSdt(),
                        phien.getTaiKhoan().getDiachi()
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
                                .toList(),
                        phien.getSanPham().getTinhtrangsp()
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
