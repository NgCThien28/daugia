package com.example.daugia.service;

import com.example.daugia.core.enums.TrangThaiThongBao;
import com.example.daugia.dto.request.ThongBaoCreationRequest;
import com.example.daugia.dto.response.NotificationDTO;
import com.example.daugia.dto.response.UserShortDTO;
import com.example.daugia.entity.Taikhoanquantri;
import com.example.daugia.entity.Thongbao;
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

    public Thongbao create(ThongBaoCreationRequest request, String email) {
        Thongbao tb = new Thongbao();
        Taikhoanquantri qtv = taikhoanquantriRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản quản trị"));
        tb.setTaiKhoanQuanTri(qtv);
//        tb.setTaiKhoan();
        tb.setNoidung(request.getNoidung());
        tb.setThoigian(Timestamp.from(Instant.now()));
        tb.setTrangthai(TrangThaiThongBao.UNSENT);
        return thongbaoRepository.save(tb);
    }

    public Thongbao update() {
        Thongbao tb = new Thongbao();
        return tb;
    }
}
