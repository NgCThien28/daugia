package com.example.daugia.controller;

import com.example.daugia.dto.request.ApiResponse;
import com.example.daugia.dto.request.TaiKhoanChangePasswordRequest;
import com.example.daugia.dto.request.TaikhoanCreationRequest;
import com.example.daugia.entity.Taikhoan;
import com.example.daugia.service.ActiveTokenService;
import com.example.daugia.service.BlacklistService;
import com.example.daugia.service.TaikhoanService;
import com.example.daugia.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/users")
public class TaikhoanController {
    @Autowired private TaikhoanService taikhoanService;
    @Autowired private BlacklistService blacklistService;
    @Autowired private ActiveTokenService activeTokenService;

    @PostMapping("/create")
    public ApiResponse<Taikhoan> createUser(@RequestBody TaikhoanCreationRequest request){
        ApiResponse<Taikhoan> apiResponse = new ApiResponse<>();
        try {
            apiResponse.setResult(taikhoanService.createUser(request));
            apiResponse.setCode(200);
            apiResponse.setMessage("Tao tai khoan thanh cong");
        } catch (IllegalArgumentException e) {
            apiResponse.setCode(400); // Mã lỗi nếu tên người dùng đã tồn tại
            apiResponse.setMessage(e.getMessage());
        }
        return apiResponse;
    }

    @GetMapping("/find-all")
    public ApiResponse<List<Taikhoan>> findAll(){
        ApiResponse<List<Taikhoan>> apiResponse = new ApiResponse<>();
        try{
            List<Taikhoan> taikhoanList = taikhoanService.findAll();
            apiResponse.setCode(200);
            apiResponse.setMessage("Thanh cong");
            apiResponse.setResult(taikhoanList);
        } catch (IllegalArgumentException e) {
            apiResponse.setCode(500);
            apiResponse.setMessage("That bai:" + e.getMessage());
        }
        return apiResponse;
    }

    @PutMapping("/update-info")
    public ApiResponse<Taikhoan> updateInfo(@RequestBody TaikhoanCreationRequest request,
                                            @RequestHeader("Authorization") String header) {
        ApiResponse<Taikhoan> response = new ApiResponse<>();
        try {
            if (header == null || !header.startsWith("Bearer ")) {
                response.setCode(401);
                response.setMessage("Thiếu token");
                return response;
            }

            String token = header.substring(7);

            // Kiểm tra token có bị vô hiệu hóa không
            if (blacklistService.isBlacklisted(token)) {
                response.setCode(401);
                response.setMessage("Token đã bị vô hiệu hóa");
                return response;
            }

            String email = JwtUtil.validateToken(token);

            if (email == null) {
                response.setCode(401);
                response.setMessage("Token không hợp lệ hoặc hết hạn");
                return response;
            }

            Taikhoan updatedUser = taikhoanService.updateInfo(request, email);
            response.setCode(200);
            response.setMessage("Cập nhật thông tin thành công");
            response.setResult(updatedUser);
        } catch (IllegalArgumentException e) {
            response.setCode(500);
            response.setMessage("Lỗi: " + e.getMessage());
        }

        return response;
    }

    @PutMapping("/change-password")
    public ApiResponse<String> changePassword(
            @RequestBody TaiKhoanChangePasswordRequest request,
            @RequestHeader("Authorization") String header) {

        ApiResponse<String> apiResponse = new ApiResponse<>();

        try {
            if (header == null || !header.startsWith("Bearer ")) {
                apiResponse.setCode(401);
                apiResponse.setMessage("Thiếu token");
                return apiResponse;
            }

            String token = header.substring(7);

            // Kiểm tra token có bị vô hiệu hóa không
            if (blacklistService.isBlacklisted(token)) {
                apiResponse.setCode(401);
                apiResponse.setMessage("Token đã bị vô hiệu hóa");
                return apiResponse;
            }

            // Lấy email từ token
            String email = JwtUtil.validateToken(token);
            if (email == null) {
                apiResponse.setCode(401);
                apiResponse.setMessage("Token không hợp lệ hoặc hết hạn");
                return apiResponse;
            }

            // Đổi mật khẩu
            taikhoanService.changePassword(request, email);

            // Vô hiệu hóa token hiện tại để bắt người dùng đăng nhập lại
            Date exp = JwtUtil.getExpiration(token);
            blacklistService.addToken(token, exp.getTime());
            activeTokenService.removeActiveToken(email);

            apiResponse.setCode(200);
            apiResponse.setMessage("Đổi mật khẩu thành công. Vui lòng đăng nhập lại");
            apiResponse.setResult("Password changed successfully");

        } catch (IllegalArgumentException e) {
            apiResponse.setCode(400);
            apiResponse.setMessage(e.getMessage());
        }

        return apiResponse;
    }
}
