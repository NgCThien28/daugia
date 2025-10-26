package com.example.daugia.service;

import com.example.daugia.dto.response.AuctionDTO;
import com.example.daugia.dto.response.BiddingDTO;
import com.example.daugia.dto.response.PaymentDTO;
import com.example.daugia.dto.response.UserShortDTO;
import com.example.daugia.entity.Phieuthanhtoan;
import com.example.daugia.repository.PhieuthanhtoanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PhieuthanhtoanService {
    @Autowired
    private PhieuthanhtoanRepository phieuthanhtoanRepository;

    public List<PaymentDTO> findAll(){
        List<Phieuthanhtoan> phieuthanhtoanList = phieuthanhtoanRepository.findAll();
        return phieuthanhtoanList.stream()
                .map(phieuthanhtoan -> new PaymentDTO(
                        phieuthanhtoan.getMatt(),
                        new UserShortDTO(phieuthanhtoan.getTaiKhoan().getMatk()),
                        new AuctionDTO(
                                phieuthanhtoan.getPhienDauGia().getMaphiendg(),
                                phieuthanhtoan.getPhienDauGia().getGiacaonhatdatduoc()
                        ),
                        phieuthanhtoan.getThoigianthanhtoan(),
                        phieuthanhtoan.getTrangthai()
                ))
                .toList();
    }
}
