package com.example.daugia.controller;

import com.example.daugia.core.custom.TokenValidator;
import com.example.daugia.core.enums.TrangThaiPhieuThanhToanTienCoc;
import com.example.daugia.dto.request.ApiResponse;
import com.example.daugia.dto.request.PhieuthanhtoantiencocCreationRequest;
import com.example.daugia.dto.request.ThongBaoCreationRequest;
import com.example.daugia.dto.response.DepositDTO;
import com.example.daugia.service.PhieuthanhtoantiencocService;
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
@RequestMapping("/deposit-payments")
public class PhieuthanhtoantiencocController {
    @Autowired
    private PhieuthanhtoantiencocService phieuthanhtoantiencocService;
    @Autowired
    private TokenValidator tokenValidator;
    @Autowired
    private ThongbaoService thongbaoService;

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

    @GetMapping("/vnpay-return")
    public ResponseEntity<?> orderReturn(HttpServletRequest request) throws JsonProcessingException {
        int result = phieuthanhtoantiencocService.orderReturn(request);
        if (result == 1) {
            return ResponseEntity.status(HttpStatus.FOUND)
                    .header("Location", "http://localhost:5173/payment-success")
                    .build();
        }

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
        Page<DepositDTO> page = phieuthanhtoantiencocService.findByAccountAndStatus(matk, status, pageable);
        return ApiResponse.success(page, "OK");
    }

    @GetMapping("/find-by-user-and-status")
    public ApiResponse<Page<DepositDTO>> findByUserAndStatus(
            @RequestHeader("Authorization") String header,
            @RequestParam TrangThaiPhieuThanhToanTienCoc status,
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 20, sort = "thoigianthanhtoan", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        String email = tokenValidator.authenticateAndGetEmail(header);
        Page<DepositDTO> page = phieuthanhtoantiencocService.findByUserAndStatus(email, status,keyword, pageable);
        return ApiResponse.success(page, "OK");
    }

    @PutMapping("/cancel-payment/{matc}")
    public ApiResponse<DepositDTO> cancelPayment(
            @RequestBody ThongBaoCreationRequest request,
            @PathVariable String matc,
            @RequestHeader("Authorization") String header) {
        String email = tokenValidator.authenticateAndGetEmail(header);
        DepositDTO depositDTO = phieuthanhtoantiencocService.cancel(matc);
        thongbaoService.createForUser(request, email, depositDTO.getTaiKhoanKhachThanhToan().getEmail());
        return ApiResponse.success(depositDTO, "Huỷ phiếu tiền cọc thành công");
    }

    @GetMapping("/export-excel")
    public void exportToExcel(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate to,
            @RequestParam(required = false)
            TrangThaiPhieuThanhToanTienCoc status,
            HttpServletResponse response) throws IOException {

        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("HH:mm:ss_dd-MM-yyyy");
        String currentDateTime = dateFormatter.format(new Date());

        response.setHeader("Content-Disposition", "attachment; filename=thongke_" + currentDateTime + ".xlsx"
        );

        phieuthanhtoantiencocService.export(from, to, status, response);
    }
}
