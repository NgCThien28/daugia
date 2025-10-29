package com.example.daugia.service;

import com.example.daugia.entity.Taikhoanquantri;
import com.example.daugia.repository.TaikhoanquantriRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaikhoanquantriService {
    @Autowired
    private TaikhoanquantriRepository taikhoanquantriRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    public List<Taikhoanquantri> findAll() {
        return taikhoanquantriRepository.findAll();
    }

    public Taikhoanquantri login(String email, String rawPassword) {
        Taikhoanquantri taikhoan = taikhoanquantriRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Email không tồn tại"));

        if (!passwordEncoder.matches(rawPassword, taikhoan.getMatkhau())) {
            throw new IllegalArgumentException("Mật khẩu không đúng");
        }

        taikhoanquantriRepository.save(taikhoan);

        taikhoan.setMatkhau(null); // Ẩn mật khẩu
        return taikhoan;
    }
}
