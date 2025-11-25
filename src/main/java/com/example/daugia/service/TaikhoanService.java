package com.example.daugia.service;

import com.example.daugia.core.enums.TrangThaiTaiKhoan;
import com.example.daugia.dto.request.TaiKhoanChangePasswordRequest;
import com.example.daugia.dto.request.TaikhoanCreationRequest;
import com.example.daugia.entity.Taikhoan;
import com.example.daugia.exception.NotFoundException;
import com.example.daugia.exception.ValidationException;
import com.example.daugia.repository.TaikhoanRepository;
import com.example.daugia.repository.ThanhphoRepository;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

@Service
public class TaikhoanService {
    @Autowired
    private TaikhoanRepository taikhoanRepository;
    @Autowired
    private ThanhphoRepository thanhphoRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private EmailService emailService;

    public List<Taikhoan> findAll() {
        return taikhoanRepository.findAll();
    }

    public Taikhoan findByEmail(String email) {
        String norm = normalizeEmail(email);
        return taikhoanRepository.findByEmail(norm)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy người dùng"));
    }

    public Taikhoan createUser(TaikhoanCreationRequest request) throws MessagingException, IOException {
        String email = normalizeEmail(request.getEmail());
        if (taikhoanRepository.existsByEmail(email)) {
            throw new ValidationException("Email đã được sử dụng");
        }

        Taikhoan tk = new Taikhoan();
        tk.setHo(request.getHo());
        tk.setTenlot(request.getTenlot());
        tk.setTen(request.getTen());
        tk.setEmail(email);
        tk.setSdt(request.getSdt());
        tk.setMatkhau(passwordEncoder.encode(request.getMatkhau()));
        tk.setTrangthaidangnhap(TrangThaiTaiKhoan.OFFLINE);
        tk.setXacthuctaikhoan(TrangThaiTaiKhoan.INACTIVE);

        // Sinh token xác thực (24h)
        String token = UUID.randomUUID().toString();
        tk.setTokenxacthuc(token);
        tk.setTokenhethan(new Timestamp(System.currentTimeMillis() + 24L * 60 * 60 * 1000));

        Taikhoan saved = taikhoanRepository.save(tk);

        emailService.sendVerificationEmail(saved, token);

        // Ẩn mật khẩu khi trả về
        saved.setMatkhau(null);
        return saved;
    }

    public boolean verifyUser(String token) {
        Taikhoan user = taikhoanRepository.findByTokenxacthuc(token)
                .orElseThrow(() -> new ValidationException("Token không hợp lệ"));

        if (user.getTokenhethan() != null &&
                user.getTokenhethan().before(new Timestamp(System.currentTimeMillis()))) {
            throw new ValidationException("Token đã hết hạn");
        }

        user.setXacthuctaikhoan(TrangThaiTaiKhoan.ACTIVE);
        user.setTokenxacthuc(null);
        user.setTokenhethan(null);
        taikhoanRepository.save(user);
        return true;
    }

    public Taikhoan login(String email, String rawPassword) {
        Taikhoan taikhoan = taikhoanRepository.findByEmail(normalizeEmail(email))
                .orElseThrow(() -> new NotFoundException("Email không tồn tại"));

        if (!passwordEncoder.matches(rawPassword, taikhoan.getMatkhau())) {
            throw new ValidationException("Mật khẩu không đúng");
        }

        if (taikhoan.getTrangthaidangnhap().equals(TrangThaiTaiKhoan.BANNED)){
             throw new ValidationException("Tài khoản hiện đã bị khoá");
        }

        taikhoan.setTrangthaidangnhap(TrangThaiTaiKhoan.ONLINE);
        taikhoanRepository.save(taikhoan);

        taikhoan.setMatkhau(null); // Ẩn mật khẩu khi trả về
        return taikhoan;
    }

    public void logout(String email) {
        Taikhoan taikhoan = taikhoanRepository.findByEmail(normalizeEmail(email))
                .orElseThrow(() -> new NotFoundException("Không tìm thấy tài khoản"));
        taikhoan.setTrangthaidangnhap(TrangThaiTaiKhoan.OFFLINE);
        taikhoanRepository.save(taikhoan);
    }

    public Taikhoan updateInfo(TaikhoanCreationRequest request, String email) {
        Taikhoan taikhoan = taikhoanRepository.findByEmail(normalizeEmail(email))
                .orElseThrow(() -> new NotFoundException("Tài khoản không tồn tại"));

        taikhoan.setThanhPho(thanhphoRepository.findById(request.getMatp())
                .orElseThrow(() -> new NotFoundException("Thành phố không tồn tại")));
        taikhoan.setHo(request.getHo());
        taikhoan.setTenlot(request.getTenlot());
        taikhoan.setTen(request.getTen());
        taikhoan.setDiachi(request.getDiachi());
        taikhoan.setDiachigiaohang(request.getDiachigiaohang());
        taikhoan.setSdt(request.getSdt());

        Taikhoan saved = taikhoanRepository.save(taikhoan);
        saved.setMatkhau(null);
        return saved;
    }

    public void changePassword(TaiKhoanChangePasswordRequest request, String email) {
        Taikhoan taikhoan = taikhoanRepository.findByEmail(normalizeEmail(email))
                .orElseThrow(() -> new NotFoundException("Tài khoản không tồn tại"));

        if (!passwordEncoder.matches(request.getMatkhaucu(), taikhoan.getMatkhau())) {
            throw new ValidationException("Mật khẩu hiện tại không đúng");
        }

        taikhoan.setMatkhau(passwordEncoder.encode(request.getMatkhaumoi()));
        taikhoanRepository.save(taikhoan);
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }
}
