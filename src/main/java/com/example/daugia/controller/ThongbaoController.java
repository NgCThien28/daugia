package com.example.daugia.controller;

import com.example.daugia.core.custom.TokenValidator;
import com.example.daugia.dto.request.ApiResponse;
import com.example.daugia.dto.response.NotificationDTO;
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
        List<NotificationDTO> thongbaoList = thongbaoService.findAll();
        return ApiResponse.success(thongbaoList, "Thành công");
    }

    @GetMapping
    public ApiResponse<Page<NotificationDTO>> findByUser(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestHeader("Authorization") String header) {
        String email = tokenValidator.authenticateAndGetEmail(header);
        Pageable pageable = PageRequest.of(page, size, Sort.by("thoigian").descending());
        Page<NotificationDTO> pageResult = thongbaoService.findByUser(email, pageable);
        return ApiResponse.success(pageResult, "Thành cônggg");
    }

    @PatchMapping("/{matb}/read")
    public ApiResponse<Void> markAsRead(@PathVariable String matb, @RequestHeader("Authorization") String header) {
        String email = tokenValidator.authenticateAndGetEmail(header);
        thongbaoService.markAsRead(matb, email);
        return ApiResponse.success(null, "Đánh dấu đã đọc thành công");
    }

    @DeleteMapping("/clear-all")
    public ApiResponse<Void> clearAll(@RequestHeader("Authorization") String header) {
        String email = tokenValidator.authenticateAndGetEmail(header);
        thongbaoService.clearAllNotifications(email);
        return ApiResponse.success(null, "Xóa tất cả thông báo thành công");
    }

    @PatchMapping("/mark-all-read")
    public ApiResponse<Void> markAllAsRead(@RequestHeader("Authorization") String header) {
        String email = tokenValidator.authenticateAndGetEmail(header);
        thongbaoService.markAllAsRead(email);
        return ApiResponse.success(null, "Đánh dấu tất cả đã đọc thành công");
    }
}