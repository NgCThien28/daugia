package com.example.daugia.service;

import com.example.daugia.dto.request.BaoCaoCreationRequest;
import com.example.daugia.entity.Baocao;
import com.example.daugia.entity.Taikhoanquantri;
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

    public List<Baocao> findAll(){
        return baocaoRepository.findAll();
    }

    public Baocao create(BaoCaoCreationRequest request) {
        Baocao baoCao = new Baocao();
        Taikhoanquantri qtv = taikhoanquantriRepository.findById(request.getMaqtv())
                .orElseThrow(()-> new IllegalArgumentException("Không tìm thấy tài khoản quản trị"));
        baoCao.setTaiKhoanQuanTri(qtv);
        baoCao.setNoidung(request.getNoidung());
        baoCao.setThoigian(Timestamp.from(Instant.now()));
        return baocaoRepository.save(baoCao);
    }

    public Baocao update(String mabc, BaoCaoCreationRequest request) {
        Baocao baoCao = baocaoRepository.findById(mabc)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy báo cáo với mã: " + mabc));
        if (request.getMaqtv() != null) {
            Taikhoanquantri qtv = taikhoanquantriRepository.findById(request.getMaqtv())
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản quản trị: " + request.getMaqtv()));
            baoCao.setTaiKhoanQuanTri(qtv);
        }

        baoCao.setNoidung(request.getNoidung());
        baoCao.setThoigian(Timestamp.from(Instant.now()));

        return baocaoRepository.save(baoCao);
    }

    public void delete(String mabc) {
        if (!baocaoRepository.existsById(mabc)) {
            throw new IllegalArgumentException("Không tìm thấy báo cáo với mã: " + mabc);
        }
        baocaoRepository.deleteById(mabc);
    }
}
