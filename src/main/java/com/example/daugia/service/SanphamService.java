package com.example.daugia.service;

import com.example.daugia.dto.response.ImageDTO;
import com.example.daugia.dto.response.ProductDTO;
import com.example.daugia.dto.response.UserShortDTO;
import com.example.daugia.entity.Sanpham;
import com.example.daugia.repository.SanphamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SanphamService {
    @Autowired
    private SanphamRepository sanphamRepository;

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
}
