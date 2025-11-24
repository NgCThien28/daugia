package com.example.daugia.controller;

import com.example.daugia.core.custom.TokenValidator;
import com.example.daugia.core.enums.TrangThaiPhieuThanhToan;
import com.example.daugia.core.enums.TrangThaiPhieuThanhToanTienCoc;
import com.example.daugia.dto.request.ApiResponse;
import com.example.daugia.dto.response.DepositDTO;
import com.example.daugia.dto.response.PaymentDTO;
import com.example.daugia.service.PhieuthanhtoanService;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/payments")
public class PhieuthanhtoanController {
    @Autowired
    private PhieuthanhtoanService phieuthanhtoanService;
    @Autowired
    private TokenValidator tokenValidator;

    @GetMapping("/find-all")
    public ApiResponse<List<PaymentDTO>> findAll() {
        ApiResponse<List<PaymentDTO>> apiResponse = new ApiResponse<>();
        try {
            List<PaymentDTO> phieuthanhtoanList = phieuthanhtoanService.findAll();
            apiResponse.setCode(200);
            apiResponse.setMessage("thanh cong");
            apiResponse.setResult(phieuthanhtoanList);
        } catch (IllegalArgumentException e) {
            apiResponse.setCode(500);
            apiResponse.setMessage("That bai:" + e.getMessage());
        }
        return apiResponse;
    }

    @GetMapping("/create-order")
    public ApiResponse<String> createOrder(HttpServletRequest request) {
        String paymentUrl = phieuthanhtoanService.createOrder(request);
        return ApiResponse.success(paymentUrl, "Tạo URL thanh toán thành công");
    }

    // Redirect endpoint giữ riêng, không dùng ApiResponse JSON
    @GetMapping("/vnpay-return")
    public ResponseEntity<?> orderReturn(HttpServletRequest request) throws JsonProcessingException {
        int result = phieuthanhtoanService.orderReturn(request);
        if (result == 1) {
            return ResponseEntity.status(HttpStatus.FOUND)
                    .header("Location", "http://localhost:5173/payment-success")
                    .build();
        }
        // 0 = thất bại/hủy, -1 = chữ ký không hợp lệ → đều đưa về trang fail
        return ResponseEntity.status(HttpStatus.FOUND)
                .header("Location", "http://localhost:5173/payment-fail")
                .build();
    }

    @GetMapping("/find-by-user-and-status")
    public ApiResponse<Page<PaymentDTO>> findByUserAndStatus(
            @RequestHeader("Authorization") String header,
            @RequestParam TrangThaiPhieuThanhToan status,
            @PageableDefault(size = 20, sort = "thoigianthanhtoan", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        String email = tokenValidator.authenticateAndGetEmail(header);
        Page<PaymentDTO> page = phieuthanhtoanService.findByUserAndStatusPaged(email, status, pageable);
        return ApiResponse.success(page, "OK");
    }
}
