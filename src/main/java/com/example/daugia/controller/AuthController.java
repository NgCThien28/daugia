package com.example.daugia.controller;

import com.example.daugia.dto.request.ApiResponse;
import com.example.daugia.dto.request.TaikhoanCreationRequest;
import com.example.daugia.entity.Taikhoan;
import com.example.daugia.service.ActiveTokenService;
import com.example.daugia.service.BlacklistService;
import com.example.daugia.service.NotificationService;
import com.example.daugia.service.TaikhoanService;
import com.example.daugia.util.JwtUtil;
import jakarta.servlet.http.HttpSession;
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

    @PostMapping("/logout")
    public ApiResponse<String> logout(@RequestHeader("Authorization") String header) {
        ApiResponse<String> response = new ApiResponse<>();

        if (header == null || !header.startsWith("Bearer ")) {
            response.setCode(400);
            response.setMessage("Thiếu token trong header");
            return response;
        }

        String token = header.substring(7);
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
}
