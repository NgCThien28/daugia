package com.example.daugia.service;

import com.example.daugia.core.enums.TrangThaiTaiKhoan;
import com.example.daugia.dto.request.TaikhoanCreationRequest;
import com.example.daugia.entity.Taikhoan;
import com.example.daugia.entity.Thanhpho;
import com.example.daugia.repository.TaikhoanRepository;
import com.example.daugia.repository.ThanhphoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaikhoanService {
    @Autowired
    private TaikhoanRepository taikhoanRepository;
    @Autowired
    private ThanhphoRepository thanhphoRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    public List<Taikhoan> findAll(){
        return taikhoanRepository.findAll();
    }

    public Taikhoan findByEmail(String email) {
        return taikhoanRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng"));
    }

    public Taikhoan createUser(TaikhoanCreationRequest request){
        Taikhoan taikhoan = new Taikhoan();
        if(taikhoanRepository.existsByEmail(request.getEmail()))
            throw new IllegalArgumentException("Username already exists");
        taikhoan.setThanhPho(thanhphoRepository.findById(request.getMatp())
                .orElseThrow(() -> new IllegalArgumentException("Thanh pho khong ton tai")));
        taikhoan.setHo(request.getHo());
        taikhoan.setTenlot(request.getTenlot());
        taikhoan.setTen(request.getTen());
        taikhoan.setEmail(request.getEmail());
        taikhoan.setDiachi(request.getDiachi());
        taikhoan.setDiachigiaohang(request.getDiachigiaohang());
        taikhoan.setSdt(request.getSdt());
        taikhoan.setMatkhau(passwordEncoder.encode(request.getMatkhau()));
        taikhoan.setTrangthaidangnhap(TrangThaiTaiKhoan.OFFLINE);
        return taikhoanRepository.save(taikhoan);
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
}
