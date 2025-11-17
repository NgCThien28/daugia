package com.example.daugia.controller;

import com.example.daugia.core.custom.TokenValidator;
import com.example.daugia.dto.request.ApiResponse;
import com.example.daugia.dto.request.TaiKhoanChangePasswordRequest;
import com.example.daugia.dto.request.TaikhoanCreationRequest;
import com.example.daugia.entity.Taikhoan;
import com.example.daugia.service.ActiveTokenService;
import com.example.daugia.service.BlacklistService;
import com.example.daugia.service.TaikhoanService;
import com.example.daugia.util.JwtUtil;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/users")
public class TaikhoanController {
    @Autowired
    private TaikhoanService taikhoanService;
    @Autowired
    private TokenValidator tokenValidator;
    @Autowired
    private BlacklistService blacklistService;
    @Autowired
    private ActiveTokenService activeTokenService;

    @PostMapping("/create")
    public ApiResponse<Taikhoan> createUser(@RequestBody TaikhoanCreationRequest request)
            throws MessagingException, IOException {
        Taikhoan created = taikhoanService.createUser(request);
        return ApiResponse.success(created, "Tạo tài khoản thành công");
    }

    // Endpoint redirect (302) – giữ try/catch để điều hướng đúng, không qua GlobalExceptionHandler
    @GetMapping("/verify")
    public ResponseEntity<?> verifyAccount(@RequestParam("token") String token) {
        try {
            boolean verified = taikhoanService.verifyUser(token);
            if (verified) {
                return ResponseEntity.status(HttpStatus.FOUND)
                        .header("Location", "http://localhost:5173/verify-success")
                        .build();
            }
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FOUND)
                    .header("Location", "http://localhost:5173/verify-fail")
                    .build();
        }
        return ResponseEntity.badRequest().build();
    }

    @GetMapping("/find-all")
    public ApiResponse<List<Taikhoan>> findAll() {
        List<Taikhoan> list = taikhoanService.findAll();
        return ApiResponse.success(list, "Thành công");
    }

    @PutMapping("/update-info")
    public ApiResponse<Taikhoan> updateInfo(@RequestBody TaikhoanCreationRequest request,
                                            @RequestHeader("Authorization") String header) {
        String email = tokenValidator.authenticateAndGetEmail(header);
        Taikhoan updated = taikhoanService.updateInfo(request, email);
        return ApiResponse.success(updated, "Cập nhật thông tin thành công");
    }

    @PutMapping("/change-password")
    public ApiResponse<String> changePassword(@RequestBody TaiKhoanChangePasswordRequest request,
                                              @RequestHeader("Authorization") String header) {
        // Lấy token và email thông qua TokenValidator
        String token = tokenValidator.extractBearerOrThrow(header);
        String email = tokenValidator.validateAndGetEmailFromToken(token);

        // Đổi mật khẩu
        taikhoanService.changePassword(request, email);

        // Vô hiệu hóa token hiện tại để bắt người dùng đăng nhập lại
        Date exp = JwtUtil.getExpiration(token);
        if (exp != null) {
            blacklistService.addToken(token, exp.getTime());
        } else {
            // TTL mặc định 60s nếu không đọc được exp
            blacklistService.addToken(token, System.currentTimeMillis() + 60_000);
        }
        activeTokenService.removeActiveToken(email);

        return ApiResponse.success("Password changed successfully", "Đổi mật khẩu thành công. Vui lòng đăng nhập lại");
    }
}
