package com.example.daugia.service;

import com.example.daugia.core.enums.TrangThaiThongBao;
import com.example.daugia.dto.request.ThongBaoCreationRequest;
import com.example.daugia.dto.response.NotificationDTO;
import com.example.daugia.dto.response.UserShortDTO;
import com.example.daugia.entity.Taikhoanquantri;
import com.example.daugia.entity.Thongbao;
import com.example.daugia.exception.NotFoundException;
import com.example.daugia.repository.TaikhoanquantriRepository;
import com.example.daugia.repository.ThongbaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

@Service
public class ThongbaoService {
    @Autowired
    private ThongbaoRepository thongbaoRepository;
    @Autowired
    private TaikhoanquantriRepository taikhoanquantriRepository;

    public List<NotificationDTO> findAll() {
        List<Thongbao> list = thongbaoRepository.findAll();
        return list.stream()
                .map(tb -> new NotificationDTO(
                        tb.getMatb(),
                        new UserShortDTO(tb.getTaiKhoanQuanTri().getMatk()),
                        new UserShortDTO(tb.getTaiKhoan().getMatk()),
                        tb.getNoidung(),
                        tb.getThoigian()
                ))
                .toList();
    }

    public Thongbao create(ThongBaoCreationRequest request, String email) {
        Taikhoanquantri admin = taikhoanquantriRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy tài khoản quản trị"));
        Thongbao tb = new Thongbao();
        tb.setTaiKhoanQuanTri(admin);
        tb.setNoidung(request.getNoidung());
        tb.setThoigian(Timestamp.from(Instant.now()));
        tb.setTrangthai(TrangThaiThongBao.UNSENT);
        return thongbaoRepository.save(tb);
    }
}
