package com.example.daugia.service;

import com.example.daugia.core.enums.TrangThaiSanPham;
import com.example.daugia.core.enums.TrangThaiTaiKhoan;
import com.example.daugia.dto.request.SanPhamCreationRequest;
import com.example.daugia.dto.response.CityDTO;
import com.example.daugia.dto.response.ImageDTO;
import com.example.daugia.dto.response.ProductDTO;
import com.example.daugia.dto.response.UserShortDTO;
import com.example.daugia.entity.Sanpham;
import com.example.daugia.entity.Taikhoan;
import com.example.daugia.entity.Taikhoanquantri;
import com.example.daugia.exception.NotFoundException;
import com.example.daugia.exception.ValidationException;
import com.example.daugia.repository.*;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static com.example.daugia.core.enums.TrangThaiSanPham.*;

@Service
public class SanphamService {
    @Autowired
    private SanphamRepository sanphamRepository;
    @Autowired
    private DanhmucRepository danhmucRepository;
    @Autowired
    private TaikhoanRepository taikhoanRepository;
    @Autowired
    private HinhanhRepository hinhanhRepository;
    @Autowired
    private ThanhphoRepository thanhphoRepository;
    @Autowired
    private TaikhoanquantriRepository taikhoanquantriRepository;
    @Autowired
    private ThongbaoService thongbaoService;

    public Page<ProductDTO> findAll(TrangThaiSanPham trangthai, Pageable pageable) {
        Specification<Sanpham> spec = (root, query, cb) -> cb.notEqual(root.get("trangthai"), TrangThaiSanPham.NOT_REGISTERED);

        if (trangthai != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("trangthai"), trangthai));
        }

        Page<Sanpham> sanphamList = sanphamRepository.findAll(spec, pageable);

        return sanphamList
                .map(sp -> new ProductDTO(
                        sp.getMasp(),
                        new UserShortDTO(
                                sp.getTaiKhoan().getMatk(),
                                sp.getTaiKhoan().getHo(),
                                sp.getTaiKhoan().getTenlot(),
                                sp.getTaiKhoan().getTen(),
                                sp.getTaiKhoan().getEmail(),
                                sp.getTaiKhoan().getSdt()
                        ),
                        new CityDTO(
                                sp.getThanhPho().getMatp(),
                                sp.getThanhPho().getTentp()
                        ),
                        sp.getHinhAnh().stream()
                                .map(ha -> new ImageDTO(ha.getMaanh(), ha.getTenanh()))
                                .toList(),
                        sp.getTinhtrangsp(),
                        sp.getTensp(),
                        sp.getTrangthai().getValue(),
                        sp.getGiamongdoi(),
                        sp.getHoahong()
                ));
    }

    public Page<Sanpham> findByUser(String email,
                                                          List<TrangThaiSanPham> statuses,
                                                          String keyword,
                                                          Pageable pageable) {
        Taikhoan taikhoan = taikhoanRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy tài khoản"));

        List<TrangThaiSanPham> effectiveStatuses =
                (statuses == null || statuses.isEmpty())
                        ? List.of(NOT_REGISTERED, PENDING_APPROVAL, APPROVED, CANCELLED)
                        : statuses;

        Specification<Sanpham> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.equal(root.get("taiKhoan").get("matk"), taikhoan.getMatk()));
            predicates.add(root.get("trangthai").in(effectiveStatuses));

            if (keyword != null && !keyword.trim().isEmpty()) {
                String keywordLower = "%" + keyword.toLowerCase() + "%";
                Predicate maspPredicate = cb.like(cb.lower(root.get("masp")), keywordLower);
                Predicate tenspPredicate = cb.like(cb.lower(root.get("tensp")), keywordLower);
                predicates.add(cb.or(maspPredicate, tenspPredicate));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        // Sử dụng findAll với Specification
        return sanphamRepository.findAll(spec, pageable);
    }

    public ProductDTO create(SanPhamCreationRequest request, String email) {
        Taikhoan taikhoan = taikhoanRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy tài khoản"));

        if (taikhoan.getXacthuctaikhoan() == TrangThaiTaiKhoan.INACTIVE) {
            throw new ValidationException("Tài khoản chưa được xác thực, vui lòng xác thực email");
        }
        if (taikhoan.getXacthuckyc() == TrangThaiTaiKhoan.INACTIVE) {
            throw new ValidationException("Tài khoản chưa được xác thực cccd, vui lòng xác thực cccd");
        }
        Sanpham sp = new Sanpham();
        sp.setTaiKhoan(taikhoan);
        sp.setDanhMuc(danhmucRepository.findById(request.getMadm())
                .orElseThrow(() -> new NotFoundException("Không tìm thấy danh mục")));
        sp.setThanhPho(thanhphoRepository.findById(request.getMatp())
                .orElseThrow(() -> new NotFoundException("Không tìm thấy thành phố")));
        sp.setTensp(request.getTensp());
        sp.setTinhtrangsp(request.getTinhtrangsp());
        sp.setTrangthai(NOT_REGISTERED);
        sp.setGiamongdoi(request.getGiamongdoi());
        sanphamRepository.save(sp);
        return convertToDTO(sp);
    }

    public ProductDTO register(String masp, String email){
        Sanpham sanpham = sanphamRepository.findById(masp)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy tài sản"));
        if(!email.equals(sanpham.getTaiKhoan().getEmail()))
            throw new ValidationException("Bạn không phải chủ sản phẩm");
        sanpham.setTrangthai(PENDING_APPROVAL);
        sanphamRepository.save(sanpham);
        return convertToDTO(sanpham);
    }

    public ProductDTO update(SanPhamCreationRequest request, String email) {
        Sanpham sanpham = sanphamRepository.findById(request.getMasp())
                .orElseThrow(() -> new NotFoundException("Không tìm thấy tài sản"));
        if(!email.equals(sanpham.getTaiKhoan().getEmail()))
            throw new ValidationException("Bạn không phải chủ sản phẩm");
        sanpham.setDanhMuc(danhmucRepository.findById(request.getMadm())
                .orElseThrow(() -> new NotFoundException("Không tìm thấy danh mục tài sản")));
        sanpham.setThanhPho(thanhphoRepository.findById(request.getMatp())
                .orElseThrow(() -> new NotFoundException("Không tìm thấy thành phố")));
        sanpham.setTensp(request.getTensp());
        sanpham.setTinhtrangsp(request.getTinhtrangsp());
        sanpham.setGiamongdoi(request.getGiamongdoi());
        sanphamRepository.save(sanpham);
        return convertToDTO(sanpham);
    }

    public String delete(String masp, String email) {
        Sanpham sanpham = sanphamRepository.findById(masp)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy tài sản"));
        if(!email.equals(sanpham.getTaiKhoan().getEmail()))
            throw new ValidationException("Bạn không phải chủ tài sản");
        if(sanpham.getTrangthai() == AUCTION_CREATED)
            throw new ValidationException("Sản phẩm đã được tạo phiên");
        sanphamRepository.delete(sanpham);
        return "Xóa sản phẩm thành công!";
    }

    public ProductDTO approveProduct(SanPhamCreationRequest request, String masp, String emailAdmin) {
        Sanpham sanpham = sanphamRepository.findById(masp)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy tài sản với mã " + masp));
        if (sanpham.getTrangthai() != TrangThaiSanPham.PENDING_APPROVAL) {
            throw new ValidationException("Sản phẩm đã được duyệt");
        }
        Taikhoanquantri admin = taikhoanquantriRepository.findByEmail(emailAdmin)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy tài khoản quản trị"));
        sanpham.setTensp(request.getTensp());
        sanpham.setTinhtrangsp(request.getTinhtrangsp());
        sanpham.setGiamongdoi(request.getGiamongdoi());
        sanpham.setHoahong(request.getHoahong());
        sanpham.setTrangthai(TrangThaiSanPham.APPROVED);
        sanpham.setTaiKhoanQuanTri(admin);

        Sanpham updatedProduct = sanphamRepository.save(sanpham);

        return convertToDTO(updatedProduct);
    }

    public ProductDTO rejectProduct(String masp, String emailAdmin) {
        Sanpham sanpham = sanphamRepository.findById(masp)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy tài sản với mã " + masp));
        if (sanpham.getTrangthai() != TrangThaiSanPham.PENDING_APPROVAL) {
            throw new ValidationException("Tài sản không ở trạng thái chờ duyệt");
        }
        Taikhoanquantri admin = taikhoanquantriRepository.findByEmail(emailAdmin)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy tài khoản quản trị"));

        sanpham.setTrangthai(TrangThaiSanPham.CANCELLED);
        sanpham.setTaiKhoanQuanTri(admin);
        Sanpham updatedProduct = sanphamRepository.save(sanpham);

        return convertToDTO(updatedProduct);
    }

    private ProductDTO convertToDTO(Sanpham sanpham) {
        UserShortDTO userShortDTO = new UserShortDTO(sanpham.getTaiKhoan().getMatk(),
                sanpham.getTaiKhoan().getEmail());
        CityDTO cityDTO = new CityDTO(sanpham.getThanhPho().getTentp());
        List<ImageDTO> hinhAnh = new ArrayList<>();

        return new ProductDTO(
                sanpham.getMasp(),
                userShortDTO,
                cityDTO,
                hinhAnh,
                sanpham.getTinhtrangsp(),
                sanpham.getTensp(),
                sanpham.getTrangthai().getValue(),
                sanpham.getGiamongdoi(),
                sanpham.getHoahong()
        );
    }

}
