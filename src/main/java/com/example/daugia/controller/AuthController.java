package com.example.daugia.controller;

import com.example.daugia.dto.request.ApiResponse;
import com.example.daugia.dto.request.TaiKhoanQuanTriCreationRequest;
import com.example.daugia.dto.request.TaikhoanCreationRequest;
import com.example.daugia.entity.Taikhoan;
import com.example.daugia.entity.Taikhoanquantri;
import com.example.daugia.service.*;
import com.example.daugia.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired private TaikhoanService taikhoanService;
    @Autowired private BlacklistService blacklistService;
    @Autowired private ActiveTokenService activeTokenService;
    @Autowired private NotificationService notificationService;
    @Autowired private TaikhoanquantriService taikhoanquantriService;

    @PostMapping("/login")
    public ApiResponse<Object> login(@RequestBody TaikhoanCreationRequest request) {
        ApiResponse<Object> response = new ApiResponse<>();

        try {
            Taikhoan user = taikhoanService.login(request.getEmail(), request.getMatkhau());

            // Kiểm tra token cũ
            String oldToken = activeTokenService.getActiveToken(user.getEmail());
            if (oldToken != null && !blacklistService.isBlacklisted(oldToken)) {
                Date expOld = JwtUtil.getExpiration(oldToken);
                blacklistService.addToken(oldToken, expOld.getTime());
                notificationService.sendLogoutEvent(user.getEmail());
            }

            // Sinh token mới
            String newToken = JwtUtil.generateToken(user.getEmail());
            activeTokenService.saveActiveToken(user.getEmail(), newToken);

            response.setCode(200);
            response.setMessage("Đăng nhập thành công");
            response.setResult(newToken);
        } catch (IllegalArgumentException e) {
            response.setCode(401);
            response.setMessage("Đăng nhập thất bại: " + e.getMessage());
        }

        return response;
    }

    @PostMapping("/login-admin")
    public ApiResponse<Object> loginAdmin(@RequestBody TaiKhoanQuanTriCreationRequest request) {
        ApiResponse<Object> response = new ApiResponse<>();
        System.out.println(request.getMatkhau());
        try {
            Taikhoanquantri user = taikhoanquantriService.login(request.getEmail(), request.getMatkhau());

            // Kiểm tra token cũ
            String oldToken = activeTokenService.getActiveToken(user.getEmail());
            if (oldToken != null && !blacklistService.isBlacklisted(oldToken)) {
                Date expOld = JwtUtil.getExpiration(oldToken);
                blacklistService.addToken(oldToken, expOld.getTime());
                notificationService.sendLogoutEvent(user.getEmail());
            }

            // Sinh token mới
            String newToken = JwtUtil.generateToken(user.getEmail());
            activeTokenService.saveActiveToken(user.getEmail(), newToken);

            response.setCode(200);
            response.setMessage("Đăng nhập thành công");
            response.setResult(newToken);
        } catch (IllegalArgumentException e) {
            response.setCode(401);
            response.setMessage("Đăng nhập thất bại: " + e.getMessage());
        }

        return response;
    }

    @GetMapping("/me")
    public ApiResponse<Object> getCurrentUser(@RequestHeader("Authorization") String header) {
        ApiResponse<Object> response = new ApiResponse<>();

        if (header == null || !header.startsWith("Bearer ")) {
            response.setCode(401);
            response.setMessage("Thiếu token");
            return response;
        }

        String token = header.substring(7);
        String email = JwtUtil.validateToken(token);

        if (email == null) {
            response.setCode(401);
            response.setMessage("Token không hợp lệ hoặc hết hạn");
            return response;
        }

        // Nếu token không phải token hợp lệ hiện tại → báo đăng nhập nơi khác
        if (!activeTokenService.isSameToken(email, token)) {
            response.setCode(403);
            response.setMessage("Tài khoản đã đăng nhập ở thiết bị khác. Vui lòng đăng nhập lại.");
            return response;
        }

        Taikhoan user = taikhoanService.findByEmail(email);
        user.setMatkhau(null);

        response.setCode(200);
        response.setMessage("Đang đăng nhập");
        response.setResult(user);
        return response;
    }

    @PostMapping("/logout")
    public ApiResponse<String> logout(@RequestHeader("Authorization") String header) {
        ApiResponse<String> response = new ApiResponse<>();

        if (header == null || !header.startsWith("Bearer ")) {
            response.setCode(400);
            response.setMessage("Thiếu token trong header");
            return response;
        }

        String token = header.substring(7);
        if (blacklistService.isBlacklisted(token)) {
            response.setCode(400);
            response.setMessage("Token đã bị vô hiệu hóa, không thể logout lại");
            return response;
        }

        String email = JwtUtil.validateToken(token);
        if (email != null) {
            taikhoanService.logout(email);
            activeTokenService.removeActiveToken(email);
        }

        Date exp = JwtUtil.getExpiration(token);
        blacklistService.addToken(token, exp.getTime());

        response.setCode(200);
        response.setMessage("Đăng xuất thành công, token đã bị vô hiệu");
        return response;
    }

    @PostMapping("/logout-admin")
    public ApiResponse<String> logoutAdmin(@RequestHeader("Authorization") String header) {
        ApiResponse<String> response = new ApiResponse<>();

        if (header == null || !header.startsWith("Bearer ")) {
            response.setCode(400);
            response.setMessage("Thiếu token trong header");
            return response;
        }

        String token = header.substring(7);
        if (blacklistService.isBlacklisted(token)) {
            response.setCode(400);
            response.setMessage("Token đã bị vô hiệu hóa, không thể logout lại");
            return response;
        }

        String email = JwtUtil.validateToken(token);
        if (email != null) {
            activeTokenService.removeActiveToken(email);
        }

        Date exp = JwtUtil.getExpiration(token);
        blacklistService.addToken(token, exp.getTime());

        response.setCode(200);
        response.setMessage("Đăng xuất thành công, token đã bị vô hiệu");
        return response;
    }
}
