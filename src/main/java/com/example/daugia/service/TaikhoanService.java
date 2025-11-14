package com.example.daugia.service;

import com.example.daugia.core.enums.TrangThaiTaiKhoan;
import com.example.daugia.dto.request.TaiKhoanChangePasswordRequest;
import com.example.daugia.dto.request.TaikhoanCreationRequest;
import com.example.daugia.entity.Taikhoan;
import com.example.daugia.repository.TaikhoanRepository;
import com.example.daugia.repository.ThanhphoRepository;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

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

    public List<Taikhoan> findAll(){
        return taikhoanRepository.findAll();
    }

    public Taikhoan findByEmail(String email) {
        return taikhoanRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng"));
    }

    public Taikhoan createUser(TaikhoanCreationRequest request) throws MessagingException, IOException {
        if (taikhoanRepository.existsByEmail(request.getEmail()))
            throw new IllegalArgumentException("Email đã được sử dụng");

        Taikhoan tk = new Taikhoan();
        tk.setHo(request.getHo());
        tk.setTenlot(request.getTenlot());
        tk.setTen(request.getTen());
        tk.setEmail(request.getEmail());
        tk.setSdt(request.getSdt());
        tk.setMatkhau(passwordEncoder.encode(request.getMatkhau()));
        tk.setTrangthaidangnhap(TrangThaiTaiKhoan.OFFLINE);
        tk.setXacthuctaikhoan(TrangThaiTaiKhoan.INACTIVE);

        // Sinh token xác thực
        String token = java.util.UUID.randomUUID().toString();
        tk.setTokenxacthuc(token);
        tk.setTokenhethan(new java.sql.Timestamp(System.currentTimeMillis() + 24 * 60 * 60 * 1000)); // 24h

        Taikhoan saved = taikhoanRepository.save(tk);

        // Gửi mail xác thực
        String verifyLink = "http://localhost:8082/api/users/verify?token=" + token;
        emailService.sendVerificationEmail(saved.getEmail(), verifyLink);

        return saved;
    }

    public boolean verifyUser(String token) {
        Taikhoan user = taikhoanRepository.findByTokenxacthuc(token)
                .orElseThrow(() -> new IllegalArgumentException("Token không hợp lệ"));

        if (user.getTokenhethan().before(new java.sql.Timestamp(System.currentTimeMillis()))) {
            throw new IllegalArgumentException("Token đã hết hạn");
        }

        user.setXacthuctaikhoan(TrangThaiTaiKhoan.ACTIVE);
        user.setTokenxacthuc(null);
        user.setTokenhethan(null);
        taikhoanRepository.save(user);

        return true;
    }

    public Taikhoan login(String email, String rawPassword) {
        Taikhoan taikhoan = taikhoanRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Email không tồn tại"));

        if (!passwordEncoder.matches(rawPassword, taikhoan.getMatkhau())) {
            throw new IllegalArgumentException("Mật khẩu không đúng");
        }

        // Cập nhật trạng thái đăng nhập
        taikhoan.setTrangthaidangnhap(TrangThaiTaiKhoan.ONLINE);
        taikhoanRepository.save(taikhoan);

        taikhoan.setMatkhau(null); // Ẩn mật khẩu
        return taikhoan;
    }

    public void logout(String email) {
        Taikhoan taikhoan = taikhoanRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản"));
        taikhoan.setTrangthaidangnhap(TrangThaiTaiKhoan.OFFLINE);
        taikhoanRepository.save(taikhoan);
    }

    public Taikhoan updateInfo (TaikhoanCreationRequest request, String email) {
        Taikhoan taikhoan = taikhoanRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Tài khoản không tồn tại"));
        taikhoan.setThanhPho(thanhphoRepository.findById(request.getMatp())
                .orElseThrow(() -> new IllegalArgumentException("Thành phố không tồn tại")));
        taikhoan.setHo(request.getHo());
        taikhoan.setTenlot(request.getTenlot());
        taikhoan.setTen(request.getTen());
        taikhoan.setDiachi(request.getDiachi());
        taikhoan.setDiachigiaohang(request.getDiachigiaohang());
        taikhoan.setSdt(request.getSdt());
        return taikhoanRepository.save(taikhoan);
    }

    public void changePassword (TaiKhoanChangePasswordRequest request, String email) {
        Taikhoan taikhoan = taikhoanRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Tài khoản không tồn tại"));
        if (!passwordEncoder.matches(request.getMatkhaucu(), taikhoan.getMatkhau())) {
            throw new IllegalArgumentException("Mật khẩu hiện tại không đúng");
        }

        taikhoan.setMatkhau(passwordEncoder.encode(request.getMatkhaumoi()));

        taikhoanRepository.save(taikhoan);
    }
}
