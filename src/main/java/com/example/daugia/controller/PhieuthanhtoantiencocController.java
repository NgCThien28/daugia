package com.example.daugia.controller;

import com.example.daugia.core.custom.TokenValidator;
import com.example.daugia.core.enums.TrangThaiPhieuThanhToanTienCoc;
import com.example.daugia.dto.request.ApiResponse;
import com.example.daugia.dto.request.PhieuthanhtoantiencocCreationRequest;
import com.example.daugia.dto.response.DepositDTO;
import com.example.daugia.service.PhieuthanhtoantiencocService;
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
@RequestMapping("/deposit-payments")
public class PhieuthanhtoantiencocController {
    @Autowired
    private PhieuthanhtoantiencocService phieuthanhtoantiencocService;
    @Autowired
    private TokenValidator tokenValidator;

    @GetMapping("/find-all")
    public ApiResponse<List<DepositDTO>> findAll() {
        List<DepositDTO> list = phieuthanhtoantiencocService.findAll();
        return ApiResponse.success(list, "Thành công");
    }

    @GetMapping("/find-by-id/{id}")
    public ApiResponse<DepositDTO> findById(@PathVariable("id") String id) {
        DepositDTO dto = phieuthanhtoantiencocService.findById(id);
        return ApiResponse.success(dto, "Thành công");
    }

    @GetMapping("/find-by-user")
    public ApiResponse<List<DepositDTO>> findByUser(@RequestHeader("Authorization") String header) {
        String email = tokenValidator.authenticateAndGetEmail(header);
        List<DepositDTO> list = phieuthanhtoantiencocService.findByUser(email);
        return ApiResponse.success(list, "Thành công");
    }

    @PostMapping("/create")
    public ApiResponse<DepositDTO> create(@RequestBody PhieuthanhtoantiencocCreationRequest request,
                                          @RequestHeader("Authorization") String header) {
        String email = tokenValidator.authenticateAndGetEmail(header);
        DepositDTO dto = phieuthanhtoantiencocService.create(request, email);
        return ApiResponse.success(dto, "Thành công");
    }

    @GetMapping("/create-order")
    public ApiResponse<String> createOrder(HttpServletRequest request) {
        String paymentUrl = phieuthanhtoantiencocService.createOrder(request);
        return ApiResponse.success(paymentUrl, "Tạo URL thanh toán thành công");
    }

    // Redirect endpoint giữ riêng, không dùng ApiResponse JSON
    @GetMapping("/vnpay-return")
    public ResponseEntity<?> orderReturn(HttpServletRequest request) throws JsonProcessingException {
        int result = phieuthanhtoantiencocService.orderReturn(request);
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

    @GetMapping("/find-by-account-and-status")
    public ApiResponse<Page<DepositDTO>> findByAccountAndStatus(
            @RequestParam String matk,
            @RequestParam TrangThaiPhieuThanhToanTienCoc status,
            @PageableDefault(size = 20, sort = "thoigianthanhtoan", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        Page<DepositDTO> page = phieuthanhtoantiencocService.findByAccountAndStatusPaged(matk, status, pageable);
        return ApiResponse.success(page, "OK");
    }

    @GetMapping("/find-by-user-and-status")
    public ApiResponse<Page<DepositDTO>> findByUserAndStatus(
            @RequestHeader("Authorization") String header,
            @RequestParam TrangThaiPhieuThanhToanTienCoc status,
            @PageableDefault(size = 20, sort = "thoigianthanhtoan", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        String email = tokenValidator.authenticateAndGetEmail(header);
        Page<DepositDTO> page = phieuthanhtoantiencocService.findByUserAndStatusPaged(email, status, pageable);
        return ApiResponse.success(page, "OK");
    }
}
