package com.example.daugia.controller;

import com.example.daugia.dto.request.ApiResponse;
import com.example.daugia.dto.request.BaoCaoCreationRequest;
import com.example.daugia.entity.Baocao;
import com.example.daugia.service.ActiveTokenService;
import com.example.daugia.service.BaocaoService;
import com.example.daugia.service.BlacklistService;
import com.example.daugia.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("reports")
public class BaocaoController {
    @Autowired
    private BaocaoService baocaoService;
    @Autowired
    private ActiveTokenService activeTokenService;
    @Autowired
    private BlacklistService blacklistService;

    @GetMapping("/find-all")
    public ApiResponse<List<Baocao>> findAll() {
        ApiResponse<List<Baocao>> apiResponse = new ApiResponse<>();
        try{
            List<Baocao> baocaoList = baocaoService.findAll();
            apiResponse.setCode(200);
            apiResponse.setMessage("Thanh cong");
            apiResponse.setResult(baocaoList);
        } catch (IllegalArgumentException e) {
            apiResponse.setCode(500);
            apiResponse.setMessage("That bai:" + e.getMessage());
        }
        return apiResponse;
    }

    @PostMapping("/create")
    public ApiResponse<Baocao> create(@RequestBody BaoCaoCreationRequest request,
                                      @RequestHeader("Authorization") String header) {
        ApiResponse<Baocao> response = new ApiResponse<>();
        try{
            if (header == null || !header.startsWith("Bearer ")) {
                response.setCode(401);
                response.setMessage("Thiếu token");
                return response;
            }

            String token = header.substring(7);
            if (blacklistService.isBlacklisted(token)) {
                response.setCode(400);
                response.setMessage("Token đã bị vô hiệu hóa, không thể tạo báo cáo");
                return response;
            }

            String email = JwtUtil.validateToken(token);

            if (email == null) {
                response.setCode(401);
                response.setMessage("Token không hợp lệ hoặc hết hạn");
                return response;
            }

            response.setResult(baocaoService.create(request, email));
            response.setCode(200);
            response.setMessage("Thanh cong");
        } catch (IllegalArgumentException e) {
            response.setCode(500);
            response.setMessage("That bai:" + e.getMessage());
        }
        return response;
    }

    @PutMapping("/update/{mabc}")
    public ApiResponse<Baocao> update(@PathVariable String mabc, @RequestBody BaoCaoCreationRequest request,
                                      @RequestHeader("Authorization") String header) {
        ApiResponse<Baocao> apiResponse = new ApiResponse<>();
        try {
            if (header == null || !header.startsWith("Bearer ")) {
                apiResponse.setCode(401);
                apiResponse.setMessage("Thiếu token");
                return apiResponse;
            }

            String token = header.substring(7);
            if (blacklistService.isBlacklisted(token)) {
                apiResponse.setCode(400);
                apiResponse.setMessage("Token đã bị vô hiệu hóa, không thể sửa báo cáo");
                return apiResponse;
            }

            String email = JwtUtil.validateToken(token);
            if (email == null) {
                apiResponse.setCode(401);
                apiResponse.setMessage("Token không hợp lệ hoặc hết hạn");
                return apiResponse;
            }

            apiResponse.setResult(baocaoService.update(mabc, request, email));
            apiResponse.setCode(200);
            apiResponse.setMessage("Cập nhật thành công");
        } catch (Exception e) {
            apiResponse.setCode(500);
            apiResponse.setMessage("Thất bại: " + e.getMessage());
        }
        return apiResponse;
    }

    @DeleteMapping("/delete/{mabc}")
    public ApiResponse<Void> delete(@PathVariable String mabc) {
        ApiResponse<Void> apiResponse = new ApiResponse<>();
        try {
            baocaoService.delete(mabc);
            apiResponse.setCode(200);
            apiResponse.setMessage("Xoá thành công");
        } catch (Exception e) {
            apiResponse.setCode(500);
            apiResponse.setMessage("Thất bại: " + e.getMessage());
        }
        return apiResponse;
    }
}
