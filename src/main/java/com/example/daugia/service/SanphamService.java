package com.example.daugia.service;

import com.example.daugia.core.enums.TrangThaiSanPham;
import com.example.daugia.dto.request.SanPhamCreationRequest;
import com.example.daugia.dto.response.ImageDTO;
import com.example.daugia.dto.response.ProductDTO;
import com.example.daugia.dto.response.UserShortDTO;
import com.example.daugia.entity.Hinhanh;
import com.example.daugia.entity.Sanpham;
import com.example.daugia.repository.DanhmucRepository;
import com.example.daugia.repository.HinhanhRepository;
import com.example.daugia.repository.SanphamRepository;
import com.example.daugia.repository.TaikhoanRepository;
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
                        sp.getHinhAnh().stream()
                                .map(ha -> new ImageDTO(ha.getMaanh(), ha.getTenanh()))
                                .toList(),
                        sp.getTinhtrangsp(),
                        sp.getTensp(),
                        sp.getTrangthai().getValue()
                ))
                .toList();
    }

    public ProductDTO create(SanPhamCreationRequest request) {
        Sanpham sp = new Sanpham();

        sp.setDanhMuc(danhmucRepository.findById(request.getMadm())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy danh mục")));
        sp.setTaiKhoan(taikhoanRepository.findById(request.getMakh())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản khách hàng")));

        sp.setTensp(request.getTensp());
        sp.setTinhtrangsp(request.getTinhtrangsp());
        sp.setTrangthai(TrangThaiSanPham.PENDING_APPROVAL);

        sanphamRepository.save(sp);

        UserShortDTO userShortDTO = new UserShortDTO(sp.getTaiKhoan().getMatk());
        List<ImageDTO> hinhAnh = new ArrayList<>();
        ProductDTO productDTO = new ProductDTO(sp.getMasp(), userShortDTO, hinhAnh, sp.getTinhtrangsp(),
                sp.getTensp(), sp.getTrangthai().getValue());

        if (request.getHinhAnh() != null && !request.getHinhAnh().isEmpty()) {
            for (String tenAnh : request.getHinhAnh()) {
                Hinhanh ha = new Hinhanh();
                ha.setTenanh(tenAnh);
                ha.setSanPham(sp);
                hinhanhRepository.save(ha);
                ImageDTO imageDTO = new ImageDTO();
                imageDTO.setTenanh(tenAnh);
//                imageDTO.setMaanh(ha.getMaanh());
                hinhAnh.add(imageDTO);
            }
        }

        return productDTO;
    }
}
