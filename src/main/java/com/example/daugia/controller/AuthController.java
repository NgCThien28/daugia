package com.example.daugia.controller;

import com.example.daugia.dto.request.ApiResponse;
import com.example.daugia.dto.request.TaikhoanCreationRequest;
import com.example.daugia.entity.Taikhoan;
import com.example.daugia.service.TaikhoanService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {
    @Autowired
    private TaikhoanService taikhoanService;

    @PostMapping("/login")
    public ApiResponse<Taikhoan> login(@RequestBody TaikhoanCreationRequest request,
                                       HttpSession session) {
        ApiResponse<Taikhoan> response = new ApiResponse<>();

        try {
            Taikhoan user = taikhoanService.login(request.getEmail(), request.getMatkhau());
            System.out.print(request.getEmail());
            System.out.print(request.getMatkhau());
            //Lưu thông tin user vào session
            session.setAttribute("user", user);

            response.setCode(200);
            response.setMessage("Đăng nhập thành công");
            response.setResult(user);
        } catch (IllegalArgumentException e) {
            response.setCode(401);
            response.setMessage("Đăng nhập thất bại: " + e.getMessage());
        }

        return response;
    }

    @GetMapping("/me")
    public ApiResponse<Object> getCurrentUser(HttpSession session) {
        ApiResponse<Object> response = new ApiResponse<>();
        Taikhoan user = (Taikhoan) session.getAttribute("user");

        if (user == null) {
            response.setCode(401);
            response.setMessage("Chưa đăng nhập");
        } else {
            response.setCode(200);
            response.setMessage("Đang đăng nhập");
            response.setResult(user);
        }

        return response;
    }

    @PostMapping("/logout")
    public ApiResponse<String> logout(HttpSession session) {
        ApiResponse<String> response = new ApiResponse<>();
        Taikhoan user = (Taikhoan) session.getAttribute("user");

        if (user != null) {
            taikhoanService.logout(user.getMatk());
            session.invalidate(); // Xóa session
            response.setCode(200);
            response.setMessage("Đăng xuất thành công");
        } else {
            response.setCode(400);
            response.setMessage("Không có người dùng nào đang đăng nhập");
        }

        return response;
    }
}
