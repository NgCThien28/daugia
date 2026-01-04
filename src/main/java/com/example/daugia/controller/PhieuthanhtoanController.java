package com.example.daugia.controller;

import com.example.daugia.core.custom.TokenValidator;
import com.example.daugia.core.enums.TrangThaiPhieuThanhToan;
import com.example.daugia.dto.request.ApiResponse;
import com.example.daugia.dto.request.ThongBaoCreationRequest;
import com.example.daugia.dto.response.PaymentDTO;
import com.example.daugia.dto.response.TransactionAmountDTO;
import com.example.daugia.dto.response.TransactionDTO;
import com.example.daugia.service.PhieuthanhtoanService;
import com.example.daugia.service.ThongbaoService;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/payments")
public class PhieuthanhtoanController {
    @Autowired
    private PhieuthanhtoanService phieuthanhtoanService;
    @Autowired
    private TokenValidator tokenValidator;
    @Autowired
    private ThongbaoService thongbaoService;

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

    @GetMapping("/vnpay-return")
    public ResponseEntity<?> orderReturn(HttpServletRequest request) throws JsonProcessingException {
        int result = phieuthanhtoanService.orderReturn(request);
        if (result == 1) {
            return ResponseEntity.status(HttpStatus.FOUND)
                    .header("Location", "http://localhost:5173/payment-success")
                    .build();
        }
        return ResponseEntity.status(HttpStatus.FOUND)
                .header("Location", "http://localhost:5173/payment-fail")
                .build();
    }

    @GetMapping("/find-by-user-and-status")
    public ApiResponse<Page<PaymentDTO>> findByUserAndStatus(
            @RequestHeader("Authorization") String header,
            @RequestParam TrangThaiPhieuThanhToan status,
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 20, sort = "thoigianthanhtoan", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        String email = tokenValidator.authenticateAndGetEmail(header);
        Page<PaymentDTO> page = phieuthanhtoanService.findByUserAndStatus(email, status, keyword, pageable);
        return ApiResponse.success(page, "OK");
    }

    //Admin
    @PutMapping("/cancel-payment/{matt}")
    public ApiResponse<PaymentDTO> cancelPayment(
            @RequestBody ThongBaoCreationRequest request,
            @PathVariable String matt,
            @RequestHeader("Authorization") String header) {
        String email = tokenValidator.authenticateAndGetEmail(header);
        PaymentDTO paymentDTO = phieuthanhtoanService.cancel(matt);
        thongbaoService.createForUser(request, email, paymentDTO.getTaiKhoanKhachThanhToan().getEmail());
        return ApiResponse.success(paymentDTO, "Huỷ phiếu thanh toán thành công");
    }

    //Admin
    @GetMapping("/export-excel")
    public void exportToExcel(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate to,
            @RequestParam(required = false)
            TrangThaiPhieuThanhToan status,
            HttpServletResponse response) throws IOException {

        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("HH:mm:ss_dd-MM-yyyy");
        String currentDateTime = dateFormatter.format(new Date());

        response.setHeader("Content-Disposition", "attachment; filename=thongke_" + currentDateTime + ".xlsx"
        );

        phieuthanhtoanService.export(from, to, status, response);
    }

    //Admin
    @GetMapping("/successful")
    public ApiResponse<List<TransactionDTO>> getSuccessfulTransactions(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate to
    ) {
        List<TransactionDTO> data = phieuthanhtoanService.getSuccessfulTransactions(from, to);
        return ApiResponse.success(data, "Lấy dữ liệu thành công");
    }

    //Admin
    @GetMapping("/total-amount")
    public ApiResponse<List<TransactionAmountDTO>> getGrossMerchandiseValue(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate to
    ) {
        List<TransactionAmountDTO> data = phieuthanhtoanService.getGrossMerchandiseValue(from, to);
        return ApiResponse.success(data, "Lấy dữ liệu thành công");
    }

    //Admin
    @GetMapping("/commission")
    public ApiResponse<List<TransactionAmountDTO>> getCommission(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate to
    ) {
        List<TransactionAmountDTO> data = phieuthanhtoanService.getCommission(from, to);
        return ApiResponse.success(data, "Lấy dữ liệu thành công");
    }
}
