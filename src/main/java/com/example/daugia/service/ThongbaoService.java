package com.example.daugia.service;

import com.example.daugia.dto.response.NotificationDTO;
import com.example.daugia.dto.response.UserShortDTO;
import com.example.daugia.entity.Thongbao;
import com.example.daugia.repository.ThongbaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ThongbaoService {
    @Autowired
    private ThongbaoRepository thongbaoRepository;

    public List<NotificationDTO> findAll(){
        List<Thongbao> thongbaoList = thongbaoRepository.findAll();
        return thongbaoList.stream()
                .map(thongbao -> new NotificationDTO(
                        thongbao.getMatb(),
                        new UserShortDTO(thongbao.getTaiKhoanQuanTri().getMatk()),
                        new UserShortDTO(thongbao.getTaiKhoan().getMatk()),
                        thongbao.getNoidung(),
                        thongbao.getThoigian()
                ))
                .toList();
    }
}
