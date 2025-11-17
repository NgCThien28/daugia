package com.example.daugia.service;

import com.example.daugia.entity.Taikhoanquantri;
import com.example.daugia.exception.NotFoundException;
import com.example.daugia.exception.ValidationException;
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
        Taikhoanquantri admin = taikhoanquantriRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Email không tồn tại"));

        if (!passwordEncoder.matches(rawPassword, admin.getMatkhau())) {
            throw new ValidationException("Mật khẩu không đúng");
        }

        admin.setMatkhau(null);
        return admin;
    }
}
