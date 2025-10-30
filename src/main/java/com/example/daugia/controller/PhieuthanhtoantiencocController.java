package com.example.daugia.controller;

import com.example.daugia.dto.request.ApiResponse;
import com.example.daugia.dto.request.PhieuthanhtoantiencocCreationRequest;
import com.example.daugia.dto.response.DepositDTO;
import com.example.daugia.entity.Phieuthanhtoantiencoc;
import com.example.daugia.service.ActiveTokenService;
import com.example.daugia.service.BlacklistService;
import com.example.daugia.service.PhieuthanhtoantiencocService;
import com.example.daugia.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/deposit-payments")
public class PhieuthanhtoantiencocController {
    @Autowired
    private PhieuthanhtoantiencocService phieuthanhtoantiencocService;
    @Autowired
    private ActiveTokenService activeTokenService;
    @Autowired
    private BlacklistService blacklistService;

    @GetMapping("/find-all")
    public ApiResponse<List<DepositDTO>> findAll(){
        ApiResponse<List<DepositDTO>> apiResponse = new ApiResponse<>();
        try {
            List<DepositDTO> depositDTOList = phieuthanhtoantiencocService.findAll();
            apiResponse.setCode(200);
            apiResponse.setMessage("thanh cong");
            apiResponse.setResult(depositDTOList);
        } catch (IllegalArgumentException e) {
            apiResponse.setCode(500);
            apiResponse.setMessage("That bai:" + e.getMessage());
        }
        return apiResponse;
    }

    @PostMapping("/create")
    public ApiResponse<DepositDTO> create(@RequestBody PhieuthanhtoantiencocCreationRequest request,
                                                     @RequestHeader("Authorization") String header) {
        ApiResponse<DepositDTO> apiResponse = new ApiResponse<>();
        try {
            if (header == null || !header.startsWith("Bearer ")) {
                apiResponse.setCode(401);
                apiResponse.setMessage("Thiếu token");
                return apiResponse;
            }

            String token = header.substring(7);
            if (blacklistService.isBlacklisted(token)) {
                apiResponse.setCode(400);
                apiResponse.setMessage("Token đã bị vô hiệu hóa, không thể tạo phieu");
                return apiResponse;
            }

            String email = JwtUtil.validateToken(token);

            if (email == null) {
                apiResponse.setCode(401);
                apiResponse.setMessage("Token không hợp lệ hoặc hết hạn");
                return apiResponse;
            }
            apiResponse.setResult(phieuthanhtoantiencocService.create(request, email));
            apiResponse.setCode(200);
            apiResponse.setMessage("Thanh cong");
        } catch (IllegalArgumentException e) {
            apiResponse.setCode(500);
            apiResponse.setMessage("That bai:" + e.getMessage());
        }
        return apiResponse;
    }

    @PutMapping("/update")
    public ApiResponse<DepositDTO> update(
            @RequestBody PhieuthanhtoantiencocCreationRequest request,
            @RequestHeader(value = "Authorization", required = false) String header) {
        ApiResponse<DepositDTO> apiResponse = new ApiResponse<>();
        try {
            // 🧩 Kiểm tra token
            if (header == null || !header.startsWith("Bearer ")) {
                apiResponse.setCode(401);
                apiResponse.setMessage("Thiếu token");
                return apiResponse;
            }
            String token = header.substring(7);
            if (blacklistService.isBlacklisted(token)) {
                apiResponse.setCode(403);
                apiResponse.setMessage("Token đã bị vô hiệu hóa, không thể cập nhật phiếu");
                return apiResponse;
            }
            String email = JwtUtil.validateToken(token);
            if (email == null) {
                apiResponse.setCode(401);
                apiResponse.setMessage("Token không hợp lệ hoặc đã hết hạn");
                return apiResponse;
            }
            DepositDTO result = phieuthanhtoantiencocService.update(request);
            apiResponse.setResult(result);
            apiResponse.setCode(200);
            apiResponse.setMessage("Cập nhật thành công");

        } catch (IllegalArgumentException e) {
            apiResponse.setCode(400);
            apiResponse.setMessage("Dữ liệu không hợp lệ: " + e.getMessage());

        } catch (IllegalStateException e) {
            apiResponse.setCode(409);
            apiResponse.setMessage("Không thể cập nhật: " + e.getMessage());

        } catch (Exception e) {
            e.printStackTrace();
            apiResponse.setCode(500);
            apiResponse.setMessage("Lỗi hệ thống: " + e.getMessage());
        }

        return apiResponse;
    }

}
