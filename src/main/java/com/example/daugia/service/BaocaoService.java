package com.example.daugia.service;

import com.example.daugia.dto.request.BaoCaoCreationRequest;
import com.example.daugia.entity.Baocao;
import com.example.daugia.entity.Taikhoanquantri;
import com.example.daugia.exception.NotFoundException;
import com.example.daugia.repository.BaocaoRepository;
import com.example.daugia.repository.TaikhoanquantriRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

@Service
public class BaocaoService {
    @Autowired
    private BaocaoRepository baocaoRepository;
    @Autowired
    private TaikhoanquantriRepository taikhoanquantriRepository;

    public List<Baocao> findAll() {
        return baocaoRepository.findAll();
    }

    public Baocao create(BaoCaoCreationRequest request, String email) {
        Taikhoanquantri qtv = taikhoanquantriRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy tài khoản quản trị"));
        Baocao bc = new Baocao();
        bc.setTaiKhoanQuanTri(qtv);
        bc.setNoidung(request.getNoidung());
        bc.setThoigian(Timestamp.from(Instant.now()));
        return baocaoRepository.save(bc);
    }

    public Baocao update(String mabc, BaoCaoCreationRequest request, String email) {
        Baocao bc = baocaoRepository.findById(mabc)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy báo cáo với mã " + mabc));

        if (email != null) {
            Taikhoanquantri qtv = taikhoanquantriRepository.findByEmail(email)
                    .orElseThrow(() -> new NotFoundException("Không tìm thấy tài khoản quản trị " + email));
            bc.setTaiKhoanQuanTri(qtv);
        }

        bc.setNoidung(request.getNoidung());
        bc.setThoigian(Timestamp.from(Instant.now()));
        return baocaoRepository.save(bc);
    }

    public void delete(String mabc) {
        if (!baocaoRepository.existsById(mabc)) {
            throw new NotFoundException("Không tìm thấy báo cáo với mã " + mabc);
        }
        baocaoRepository.deleteById(mabc);
    }
}
