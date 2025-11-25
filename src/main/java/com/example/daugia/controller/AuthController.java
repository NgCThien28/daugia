package com.example.daugia.controller;

import com.example.daugia.core.custom.TokenValidator;
import com.example.daugia.dto.request.ApiResponse;
import com.example.daugia.dto.request.TaiKhoanQuanTriCreationRequest;
import com.example.daugia.dto.request.TaikhoanCreationRequest;
import com.example.daugia.entity.Taikhoan;
import com.example.daugia.entity.Taikhoanquantri;
import com.example.daugia.exception.ForbiddenException;
import com.example.daugia.service.*;
import com.example.daugia.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

@RestController
@RequestMapping("/auth")
public class AuthController {
    @Autowired
    private TaikhoanService taikhoanService;
    @Autowired
    private BlacklistService blacklistService;
    @Autowired
    private TokenValidator tokenValidator;
    @Autowired
    private ActiveTokenService activeTokenService;
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private TaikhoanquantriService taikhoanquantriService;

    @PostMapping("/login")
    public ApiResponse<Object> login(@RequestBody TaikhoanCreationRequest request) {

        Taikhoan user = taikhoanService.login(request.getEmail(), request.getMatkhau());

        invalidateOldActiveTokenIfExists(user.getEmail());

        String newToken = JwtUtil.generateToken(user.getEmail());
        activeTokenService.saveActiveToken(user.getEmail(), newToken);

        return ApiResponse.success(newToken, "Đăng nhập thành công");
    }

    @PostMapping("/login-admin")
    public ApiResponse<Object> loginAdmin(@RequestBody TaiKhoanQuanTriCreationRequest request) {
        Taikhoanquantri admin = taikhoanquantriService.login(request.getEmail(), request.getMatkhau());

        invalidateOldActiveTokenIfExists(admin.getEmail());

        String newToken = JwtUtil.generateToken(admin.getEmail());
        activeTokenService.saveActiveToken(admin.getEmail(), newToken);

        return ApiResponse.success(newToken, "Đăng nhập thành công");
    }

    @GetMapping("/me")
    public ApiResponse<Object> getCurrentUser(@RequestHeader("Authorization") String header) {
        String token = tokenValidator.extractBearerOrThrow(header);
        String email = tokenValidator.validateAndGetEmailFromToken(token);

        // Token hợp lệ nhưng không phải token phiên hiện tại → bị đăng nhập ở nơi khác
        if (!activeTokenService.isSameToken(email, token)) {
            throw new ForbiddenException("Tài khoản đã đăng nhập ở thiết bị khác. Vui lòng đăng nhập lại.");
        }

        Taikhoan user = taikhoanService.findByEmail(email);
        user.setMatkhau(null);
        return ApiResponse.success(user, "Đang đăng nhập");
    }

    @GetMapping("/me-admin")
    public ApiResponse<Object> getCurrentAdmin(@RequestHeader("Authorization") String header) {
        String token = tokenValidator.extractBearerOrThrow(header);
        String email = tokenValidator.validateAndGetEmailFromToken(token);

        Taikhoanquantri admin = taikhoanquantriService.findByEmail(email);
        admin.setMatkhau(null);
        return ApiResponse.success(admin, "Đang đăng nhập");
    }

    @PostMapping("/logout")
    public ApiResponse<String> logout(@RequestHeader("Authorization") String header) {
        String token = tokenValidator.extractBearerOrThrow(header);

        // Nếu đã blacklisted thì không cho logout lại
        if (blacklistService.isBlacklisted(token)) {
            return ApiResponse.error(400, "Token đã bị vô hiệu hóa, không thể logout lại");
        }

        String email = JwtUtil.validateToken(token);
        if (email != null) {
            taikhoanService.logout(email);
            activeTokenService.removeActiveToken(email);
            notificationService.sendLogoutEvent(email, true);
        }

        blacklistToken(token);
        return ApiResponse.success("OK", "Đăng xuất thành công, token đã bị vô hiệu");
    }

    @PostMapping("/logout-admin")
    public ApiResponse<String> logoutAdmin(@RequestHeader("Authorization") String header) {
        String token = tokenValidator.extractBearerOrThrow(header);

        if (blacklistService.isBlacklisted(token)) {
            return ApiResponse.error(400, "Token đã bị vô hiệu hóa, không thể logout lại");
        }

        String email = JwtUtil.validateToken(token);
        if (email != null) {
            activeTokenService.removeActiveToken(email);
            notificationService.sendLogoutEvent(email, true);
        }

        blacklistToken(token);
        return ApiResponse.success("OK", "Đăng xuất thành công, token đã bị vô hiệu");
    }

    private void invalidateOldActiveTokenIfExists(String email) {
        String oldToken = activeTokenService.getActiveToken(email);
        if (oldToken != null && !blacklistService.isBlacklisted(oldToken)) {
            Date expOld = JwtUtil.getExpiration(oldToken);
            if (expOld != null) {
                blacklistService.addToken(oldToken, expOld.getTime());
            } else {
                // TTL fallback nếu không lấy được exp
                blacklistService.addToken(oldToken, System.currentTimeMillis() + 60_000);
            }
            notificationService.sendLogoutEvent(email, false);
        }
    }

    private void blacklistToken(String token) {
        Date exp = JwtUtil.getExpiration(token);
        long blacklistUntil = (exp != null) ? exp.getTime() : (System.currentTimeMillis() + 60_000);
        blacklistService.addToken(token, blacklistUntil);
    }
}
