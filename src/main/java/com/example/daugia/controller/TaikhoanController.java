package com.example.daugia.controller;

import com.example.daugia.core.custom.TokenValidator;
import com.example.daugia.dto.request.ApiResponse;
import com.example.daugia.dto.request.TaiKhoanChangePasswordRequest;
import com.example.daugia.dto.request.TaikhoanCreationRequest;
import com.example.daugia.dto.response.UserShortDTO;
import com.example.daugia.entity.Taikhoan;
import com.example.daugia.service.ActiveTokenService;
import com.example.daugia.service.BlacklistService;
import com.example.daugia.service.TaikhoanService;
import com.example.daugia.util.JwtUtil;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
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

    //Admin
    @GetMapping("/find-all")
    public ApiResponse<Page<UserShortDTO>> find_All(
            @PageableDefault(size = 12, sort = "matk", direction = Sort.Direction.ASC)
            Pageable pageable) {
        Page<UserShortDTO> list = taikhoanService.findAll(pageable);
        return ApiResponse.success(list, "Thành công");
    }

    //Admin
    @GetMapping("/{matk}")
    public ApiResponse<Taikhoan> findById(@PathVariable String matk){
        Taikhoan taikhoan = taikhoanService.findById(matk);
        return ApiResponse.success(taikhoan, "Thành công");
    }

    @PostMapping("/create")
    public ApiResponse<Taikhoan> createUser(@RequestBody TaikhoanCreationRequest request)
            throws MessagingException, IOException {
        Taikhoan created = taikhoanService.createUser(request);
        return ApiResponse.success(created, "Tạo tài khoản thành công");
    }

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

    @PostMapping("/resend-verify-code")
    public ApiResponse<String> resendVerifyEmail(@RequestHeader("Authorization") String header) throws MessagingException, IOException {
        String email = tokenValidator.authenticateAndGetEmail(header);
        taikhoanService.resendVerificationEmail(email);
        return ApiResponse.success("OK", "Đã gửi email xác thực đến tài khoản email của bạn");
    }

    @PutMapping(value = "/update-info", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<Taikhoan> updateInfo(@RequestPart("request")  TaikhoanCreationRequest request,
                                            @RequestHeader("Authorization") String header,
                                            @RequestParam(value = "files", required = false) MultipartFile[] files) {
        List<MultipartFile> list = (files == null) ? List.of() : Arrays.asList(files);
        String email = tokenValidator.authenticateAndGetEmail(header);
        Taikhoan updated = taikhoanService.updateInfo(request, email, list);
        return ApiResponse.success(updated, "Cập nhật thông tin thành công");
    }

    @PutMapping("/change-password")
    public ApiResponse<String> changePassword(@RequestBody TaiKhoanChangePasswordRequest request,
                                              @RequestHeader("Authorization") String header) {
        String token = tokenValidator.extractBearerOrThrow(header);
        String email = tokenValidator.validateAndGetEmailFromToken(token);

        taikhoanService.changePassword(request, email);

        // Vo hieu token
        Date exp = JwtUtil.getExpiration(token);
        if (exp != null) {
            blacklistService.addToken(token, exp.getTime());
        } else {
            // TTL mac dinh 60s neu khong co exp
            blacklistService.addToken(token, System.currentTimeMillis() + 60_000);
        }
        activeTokenService.removeActiveToken(email);

        return ApiResponse.success("Password changed successfully", "Đổi mật khẩu thành công. Vui lòng đăng nhập lại");
    }
}
