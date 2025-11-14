package com.example.daugia.controller;

import com.example.daugia.dto.request.ApiResponse;
import com.example.daugia.dto.request.PhieuthanhtoantiencocCreationRequest;
import com.example.daugia.dto.response.DepositDTO;
import com.example.daugia.service.ActiveTokenService;
import com.example.daugia.service.BlacklistService;
import com.example.daugia.service.PhieuthanhtoantiencocService;
import com.example.daugia.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    @GetMapping("/find-by-id/{id}")
    public ApiResponse<DepositDTO> findById(@PathVariable("id") String id){
        ApiResponse<DepositDTO> apiResponse = new ApiResponse<>();
        try {
            DepositDTO depositDTO = phieuthanhtoantiencocService.findById(id);
            apiResponse.setCode(200);
            apiResponse.setMessage("thanh cong");
            apiResponse.setResult(depositDTO);
        } catch (IllegalArgumentException e) {
            apiResponse.setCode(500);
            apiResponse.setMessage("That bai:" + e.getMessage());
        }
        return apiResponse;
    }
    
    @GetMapping("/find-by-user")
    public ApiResponse<List<DepositDTO>> findByUser(@RequestHeader("Authorization") String header) {
        ApiResponse<List<DepositDTO>> apiResponse = new ApiResponse<>();
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
            apiResponse.setResult(phieuthanhtoantiencocService.findByUser(email));
            apiResponse.setCode(200);
            apiResponse.setMessage("Thanh cong");
        } catch (Exception e) {
            throw new RuntimeException(e);
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
            apiResponse.setCode(400);
            apiResponse.setMessage(e.getMessage());

        } catch (IllegalStateException e) {
            apiResponse.setCode(409);
            apiResponse.setMessage(e.getMessage());

        } catch (Exception e) {
            e.printStackTrace();
            apiResponse.setCode(500);
            apiResponse.setMessage("Lỗi hệ thống xin đợi trong giây lát");
        }
        return apiResponse;
    }

    @GetMapping("/create-order")
    public ApiResponse<String> createOrder(HttpServletRequest request) {
        ApiResponse<String> apiResponse = new ApiResponse<>();
        try {
            String paymentUrl = phieuthanhtoantiencocService.createOrder(request);
            apiResponse.setCode(200);
            apiResponse.setMessage("Tạo URL thanh toán thành công");
            apiResponse.setResult(paymentUrl);
        } catch (IllegalStateException e) {
            apiResponse.setCode(400);
            apiResponse.setMessage(e.getMessage());
        } catch (Exception e) {
            apiResponse.setCode(500);
            apiResponse.setMessage("Lỗi hệ thống: " + e.getMessage());
        }
        return apiResponse;
    }

    @GetMapping("/vnpay-return")
    public ResponseEntity<?> orderReturn(HttpServletRequest request) {
        ApiResponse<String> apiResponse = new ApiResponse<>();
        try {
            int result = phieuthanhtoantiencocService.orderReturn(request);
            if (result == 1) {
                 return ResponseEntity.status(HttpStatus.FOUND)
                        .header("Location", "http://localhost:5173/payment-success")
                        .build();
            } else if (result == 0) {
                apiResponse.setCode(400);
                apiResponse.setMessage("Thanh toán thất bại hoặc bị hủy");
            } else {
                apiResponse.setCode(401);
                apiResponse.setMessage("Chữ ký không hợp lệ (Invalid signature)");
            }
        } catch (IllegalArgumentException e) {
            apiResponse.setCode(400);
            apiResponse.setMessage("Dữ liệu không hợp lệ: " + e.getMessage());
        } catch (IllegalStateException e) {
            apiResponse.setCode(409);
            apiResponse.setMessage("Lỗi trạng thái: " + e.getMessage());
        } catch (Exception e) {
            apiResponse.setCode(500);
            apiResponse.setMessage("Lỗi hệ thống: " + e.getMessage());
            e.printStackTrace();
        }
        return ResponseEntity.badRequest().build();
    }
}
