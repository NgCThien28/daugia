package com.example.daugia.service;

import com.example.daugia.core.enums.TrangThaiTaiKhoan;
import com.example.daugia.entity.Taikhoan;
import com.example.daugia.entity.Taikhoanquantri;
import com.example.daugia.exception.NotFoundException;
import com.example.daugia.exception.ValidationException;
import com.example.daugia.repository.TaikhoanRepository;
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
    @Autowired
    private TaikhoanRepository taikhoanRepository;

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

    public Taikhoan bannedUser(String matk){
        Taikhoan taikhoan = taikhoanRepository.findById(matk).
                orElseThrow(() -> new NotFoundException("Không tìm thấy người dùng"));

        if (taikhoan.getTrangthaidangnhap() == TrangThaiTaiKhoan.BANNED) {
            throw new ValidationException("Tài khoản này đã bị khóa rồi");
        }
        taikhoan.setTrangthaidangnhap(TrangThaiTaiKhoan.BANNED);
        return taikhoanRepository.save(taikhoan);
    }

    public Taikhoan unBanUser(String matk){
        Taikhoan taikhoan = taikhoanRepository.findById(matk).
                orElseThrow(() -> new NotFoundException("Không tìm thấy người dùng"));

        if (taikhoan.getTrangthaidangnhap() != TrangThaiTaiKhoan.BANNED) {
            throw new ValidationException("Tài khoản này không bị khoá");
        }

        taikhoan.setTrangthaidangnhap(TrangThaiTaiKhoan.OFFLINE);
        return taikhoanRepository.save(taikhoan);
    }

    public Taikhoanquantri findByEmail(String email) {
        String norm = normalizeEmail(email);
        return taikhoanquantriRepository.findByEmail(norm)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy người dùng"));
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }
}
