package com.example.daugia.service;

import com.example.daugia.core.enums.TrangThaiSanPham;
import com.example.daugia.dto.request.SanPhamCreationRequest;
import com.example.daugia.dto.response.CityDTO;
import com.example.daugia.dto.response.ImageDTO;
import com.example.daugia.dto.response.ProductDTO;
import com.example.daugia.dto.response.UserShortDTO;
import com.example.daugia.entity.Sanpham;
import com.example.daugia.entity.Taikhoan;
import com.example.daugia.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

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
                        new CityDTO(sp.getThanhPho().getMatp(), sp.getThanhPho().getTentp()),
                        sp.getHinhAnh().stream()
                                .map(ha -> new ImageDTO(ha.getMaanh(), ha.getTenanh()))
                                .toList(),
                        sp.getTinhtrangsp(),
                        sp.getTensp(),
                        sp.getTrangthai().getValue()
                ))
                .toList();
    }

    public List<Sanpham> findByUser(String email){
        Taikhoan taikhoan = taikhoanRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản"));
        return sanphamRepository.findByTaiKhoan_Matk(taikhoan.getMatk());
    }

    public ProductDTO create(SanPhamCreationRequest request, String email) {
        Sanpham sp = new Sanpham();

        sp.setDanhMuc(danhmucRepository.findById(request.getMadm())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy danh mục")));
        sp.setTaiKhoan(taikhoanRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản khách hàng")));
        sp.setThanhPho(thanhphoRepository.findById(request.getMatp())
                .orElseThrow(() -> new IllegalArgumentException("Khong tim thay thanh pho")));
        sp.setTensp(request.getTensp());
        sp.setTinhtrangsp(request.getTinhtrangsp());
        sp.setTrangthai(TrangThaiSanPham.PENDING_APPROVAL);

        sanphamRepository.save(sp);

        UserShortDTO userShortDTO = new UserShortDTO(sp.getTaiKhoan().getMatk());
        CityDTO cityDTO = new CityDTO(sp.getThanhPho().getTentp());
        List<ImageDTO> hinhAnh = new ArrayList<>();

        return new ProductDTO(sp.getMasp(), userShortDTO,cityDTO, hinhAnh, sp.getTinhtrangsp(),
                sp.getTensp(), sp.getTrangthai().getValue());
    }

    public ProductDTO update (SanPhamCreationRequest request) {
        Sanpham sanpham = sanphamRepository.findById(request.getMasp())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm"));
        sanpham.setDanhMuc(danhmucRepository.findById(request.getMadm())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy danh mục sản phẩm")));
        sanpham.setThanhPho(thanhphoRepository.findById(request.getMatp())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thành phố")));
        sanpham.setTensp(request.getTensp());
        sanpham.setTinhtrangsp(request.getTinhtrangsp());

        UserShortDTO userShortDTO = new UserShortDTO(sanpham.getTaiKhoan().getMatk());
        CityDTO cityDTO = new CityDTO(sanpham.getThanhPho().getTentp());
        List<ImageDTO> hinhAnh = new ArrayList<>();
        ProductDTO productDTO = new ProductDTO(sanpham.getMasp(), userShortDTO,cityDTO, hinhAnh, sanpham.getTinhtrangsp(),
                sanpham.getTensp(), sanpham.getTrangthai().getValue());

        sanphamRepository.save(sanpham);
        return productDTO;
    }
}
