package com.example.daugia.controller;

import com.example.daugia.core.custom.TokenValidator;
import com.example.daugia.dto.request.ApiResponse;
import com.example.daugia.dto.response.NotificationDTO;
import com.example.daugia.entity.Thongbao;
import com.example.daugia.service.ThongbaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notifications")
public class ThongbaoController {
    @Autowired
    private ThongbaoService thongbaoService;
    @Autowired
    private TokenValidator tokenValidator;
    @GetMapping("/find-all")
    public ApiResponse<List<NotificationDTO>> findAll() {
        ApiResponse<List<NotificationDTO>> apiResponse = new ApiResponse<>();
        try {
            List<NotificationDTO> thongbaoList = thongbaoService.findAll();
            apiResponse.setCode(200);
            apiResponse.setMessage("thanh cong");
            apiResponse.setResult(thongbaoList);
        } catch (IllegalArgumentException e) {
            apiResponse.setCode(500);
            apiResponse.setMessage("That bai:" + e.getMessage());
        }
        return apiResponse;
    }

    @GetMapping
    public ApiResponse<Page<NotificationDTO>> findByUser(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestHeader("Authorization") String header) {
        ApiResponse<Page<NotificationDTO>> apiResponse = new ApiResponse<>();
        try {
            String email = tokenValidator.authenticateAndGetEmail(header);
            Pageable pageable = PageRequest.of(page, size, Sort.by("thoigian").descending());
            Page<NotificationDTO> pageResult = thongbaoService.findByUser(email, pageable);
            apiResponse.setCode(200);
            apiResponse.setMessage("Thành công");
            apiResponse.setResult(pageResult);
        } catch (Exception e) {
            apiResponse.setCode(500);
            apiResponse.setMessage("Thất bại: " + e.getMessage());
        }
        return apiResponse;
    }

    @PatchMapping("/{matb}/read")
    public ApiResponse<Void> markAsRead(@PathVariable String matb, @RequestHeader("Authorization") String header) {
        ApiResponse<Void> apiResponse = new ApiResponse<>();
        try {
            String email = tokenValidator.authenticateAndGetEmail(header);
            thongbaoService.markAsRead(matb, email);
            apiResponse.setCode(200);
            apiResponse.setMessage("Đánh dấu đã đọc thành công");
            apiResponse.setResult(null);
        } catch (Exception e) {
            apiResponse.setCode(500);
            apiResponse.setMessage("Thất bại: " + e.getMessage());
        }
        return apiResponse;
    }
}
