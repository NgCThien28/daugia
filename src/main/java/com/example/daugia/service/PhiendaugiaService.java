package com.example.daugia.service;

import com.example.daugia.dto.response.AuctionDTO;
import com.example.daugia.dto.response.UserShortDTO;
import com.example.daugia.entity.Phiendaugia;
import com.example.daugia.repository.PhiendaugiaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PhiendaugiaService {
    @Autowired
    private PhiendaugiaRepository phiendaugiaRepository;

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
}
