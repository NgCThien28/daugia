package com.example.daugia.service;

import com.example.daugia.dto.response.AuctionDTO;
import com.example.daugia.dto.response.BiddingDTO;
import com.example.daugia.dto.response.UserShortDTO;
import com.example.daugia.entity.Phientragia;
import com.example.daugia.repository.PhientragiaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PhientragiaService {
    @Autowired
    private PhientragiaRepository phientragiaRepository;

    public List<BiddingDTO> findAll(){
        List<Phientragia> phientragiaList = phientragiaRepository.findAll();
        return phientragiaList.stream()
                .map(phientragia -> new BiddingDTO(
                        phientragia.getMaphientg(),
                        new UserShortDTO(phientragia.getTaiKhoan().getMatk()),
                        new AuctionDTO(phientragia.getPhienDauGia().getMaphiendg()),
                        phientragia.getSotien(),
                        phientragia.getSolan(),
                        phientragia.getThoigian(),
                        phientragia.getThoigiancho()
                ))
                .toList();
    }
}
