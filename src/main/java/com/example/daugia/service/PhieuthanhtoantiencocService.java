package com.example.daugia.service;

import com.example.daugia.dto.response.AuctionDTO;
import com.example.daugia.dto.response.DepositDTO;
import com.example.daugia.dto.response.UserShortDTO;
import com.example.daugia.entity.Phieuthanhtoantiencoc;
import com.example.daugia.repository.PhieuthanhtoantiencocRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PhieuthanhtoantiencocService {
    @Autowired
    public PhieuthanhtoantiencocRepository phieuthanhtoantiencocRepository;

    public List<DepositDTO> findAll() {
        List<Phieuthanhtoantiencoc> phieuthanhtoantiencocList = phieuthanhtoantiencocRepository.findAll();
        return phieuthanhtoantiencocList.stream()
                .map(phieuthanhtoantiencoc -> new DepositDTO(
                        phieuthanhtoantiencoc.getMatc(),
                        new UserShortDTO(phieuthanhtoantiencoc.getTaiKhoan().getMatk()),
                        new AuctionDTO(
                                phieuthanhtoantiencoc.getPhienDauGia().getMaphiendg(),
                                phieuthanhtoantiencoc.getPhienDauGia().getGiacaonhatdatduoc()
                        ),
                        phieuthanhtoantiencoc.getThoigianthanhtoan(),
                        phieuthanhtoantiencoc.getTrangthai()
                ))
                .toList();
    }
}
