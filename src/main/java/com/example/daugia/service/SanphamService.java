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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    public List<ProductDTO> findAll() {
        List<Sanpham> sanphamList = sanphamRepository.findAll();
        return sanphamList.stream()
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
                        sp.getTrangthai().getValue()
                ))
                .toList();
    }

    // Mặc định lọc 3 trạng thái: PENDING_APPROVAL, APPROVED, CANCELLED
    public Page<Sanpham> findByUser(String email, Pageable pageable) {
        List<TrangThaiSanPham> defaultStatuses = List.of(PENDING_APPROVAL, APPROVED, CANCELLED);
        return findByUserWithStatuses(email, defaultStatuses, pageable);
    }

    public Page<Sanpham> findByUserWithStatuses(String email,
                                                List<TrangThaiSanPham> statuses,
                                                Pageable pageable) {
        Taikhoan taikhoan = taikhoanRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy tài khoản"));

        List<TrangThaiSanPham> effectiveStatuses =
                (statuses == null || statuses.isEmpty())
                        ? List.of(PENDING_APPROVAL, APPROVED, CANCELLED)
                        : statuses;

        return sanphamRepository.findByTaiKhoan_MatkAndTrangthaiIn(
                taikhoan.getMatk(), effectiveStatuses, pageable
        );
    }

    public ProductDTO create(SanPhamCreationRequest request, String email) {
        Taikhoan taikhoan = taikhoanRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy tài khoản"));

        if (taikhoan.getXacthuctaikhoan() == TrangThaiTaiKhoan.INACTIVE) {
            throw new ValidationException("Tài khoản chưa được xác thực, vui lòng xác thực email");
        }

        Sanpham sp = new Sanpham();
        sp.setTaiKhoan(taikhoan);
        sp.setDanhMuc(danhmucRepository.findById(request.getMadm())
                .orElseThrow(() -> new NotFoundException("Không tìm thấy danh mục")));
        sp.setThanhPho(thanhphoRepository.findById(request.getMatp())
                .orElseThrow(() -> new NotFoundException("Không tìm thấy thành phố")));
        sp.setTensp(request.getTensp());
        sp.setTinhtrangsp(request.getTinhtrangsp());
        sp.setTrangthai(PENDING_APPROVAL);
        sp.setGiamongdoi(request.getGiamongdoi());
        sanphamRepository.save(sp);

        UserShortDTO userShortDTO = new UserShortDTO(sp.getTaiKhoan().getMatk());
        CityDTO cityDTO = new CityDTO(sp.getThanhPho().getTentp());
        List<ImageDTO> hinhAnh = new ArrayList<>();

        return new ProductDTO(
                sp.getMasp(), userShortDTO, cityDTO, hinhAnh,
                sp.getTinhtrangsp(), sp.getTensp(), sp.getTrangthai().getValue()
        );
    }

    public ProductDTO update(SanPhamCreationRequest request, String email) {
        Sanpham sanpham = sanphamRepository.findById(request.getMasp())
                .orElseThrow(() -> new NotFoundException("Không tìm thấy sản phẩm"));
        if(!email.equals(sanpham.getTaiKhoan().getEmail()))
            throw new ValidationException("Bạn không phải chủ sản phẩm");
        sanpham.setDanhMuc(danhmucRepository.findById(request.getMadm())
                .orElseThrow(() -> new NotFoundException("Không tìm thấy danh mục sản phẩm")));
        sanpham.setThanhPho(thanhphoRepository.findById(request.getMatp())
                .orElseThrow(() -> new NotFoundException("Không tìm thấy thành phố")));
        sanpham.setTensp(request.getTensp());
        sanpham.setTinhtrangsp(request.getTinhtrangsp());
        sanpham.setGiamongdoi(request.getGiamongdoi());
        sanphamRepository.save(sanpham);

        UserShortDTO userShortDTO = new UserShortDTO(sanpham.getTaiKhoan().getMatk());
        CityDTO cityDTO = new CityDTO(sanpham.getThanhPho().getTentp());
        List<ImageDTO> hinhAnh = new ArrayList<>();

        return new ProductDTO(
                sanpham.getMasp(), userShortDTO, cityDTO, hinhAnh,
                sanpham.getTinhtrangsp(), sanpham.getTensp(), sanpham.getTrangthai().getValue()
        );
    }

    public String delete(String masp, String email) {
        Sanpham sanpham = sanphamRepository.findById(masp)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy sản phẩm"));
        if(!email.equals(sanpham.getTaiKhoan().getEmail()))
            throw new ValidationException("Bạn không phải chủ sản phẩm");
        if(sanpham.getTrangthai() == AUCTION_CREATED)
            throw new ValidationException("Sản phẩm đã được tạo phiên");
        sanphamRepository.delete(sanpham);
        return "Xóa sản phẩm thành công!";
    }

    public ProductDTO approveProduct(String masp, String emailAdmin) {
        Sanpham sanpham = sanphamRepository.findById(masp)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy sản phẩm với mã: " + masp));
        if (sanpham.getTrangthai() != TrangThaiSanPham.PENDING_APPROVAL) {
            throw new ValidationException("Sản phẩm đã được duyệt");
        }
        Taikhoanquantri admin = taikhoanquantriRepository.findByEmail(emailAdmin)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy tài khoản quản trị"));

        sanpham.setTrangthai(TrangThaiSanPham.APPROVED);
        sanpham.setTaiKhoanQuanTri(admin);

        Sanpham updatedProduct = sanphamRepository.save(sanpham);

        return convertToDTO(updatedProduct);
    }

    public ProductDTO rejectProduct(String masp, String emailAdmin) {
        Sanpham sanpham = sanphamRepository.findById(masp)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy sản phẩm với mã: " + masp));
        if (sanpham.getTrangthai() != TrangThaiSanPham.PENDING_APPROVAL) {
            throw new ValidationException("Sản phẩm không ở trạng thái chờ duyệt");
        }
        Taikhoanquantri admin = taikhoanquantriRepository.findByEmail(emailAdmin)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy tài khoản quản trị"));

        sanpham.setTrangthai(TrangThaiSanPham.CANCELLED);
        sanpham.setTaiKhoanQuanTri(admin);

        Sanpham updatedProduct = sanphamRepository.save(sanpham);

        return convertToDTO(updatedProduct);
    }

    private ProductDTO convertToDTO(Sanpham sanpham) {
        UserShortDTO userShortDTO = new UserShortDTO(sanpham.getTaiKhoan().getMatk());
        CityDTO cityDTO = new CityDTO(sanpham.getThanhPho().getTentp());
        List<ImageDTO> hinhAnh = new ArrayList<>();

        return new ProductDTO(
                sanpham.getMasp(),
                userShortDTO,
                cityDTO,
                hinhAnh,
                sanpham.getTinhtrangsp(),
                sanpham.getTensp(),
                sanpham.getTrangthai().getValue()
        );
    }

}
