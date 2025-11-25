package com.example.daugia.controller;

import com.example.daugia.dto.request.ApiResponse;
import com.example.daugia.entity.Taikhoan;
import com.example.daugia.entity.Taikhoanquantri;
import com.example.daugia.service.NotificationService;
import com.example.daugia.service.TaikhoanquantriService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admins")
public class TaikhoanquantriController {
    @Autowired
    private TaikhoanquantriService taikhoanquantriService;
    @Autowired
    private NotificationService notificationService;

    @GetMapping("/find-all")
    public ApiResponse<List<Taikhoanquantri>> findAll() {
        List<Taikhoanquantri> list = taikhoanquantriService.findAll();
        return ApiResponse.success(list, "Thành công");
    }

    @PutMapping("/banned-user/{matk}")
    public ApiResponse<Taikhoan> bannedUser(@PathVariable String matk) {
        Taikhoan taikhoan = taikhoanquantriService.bannedUser(matk);
        notificationService.sendBanEvent(taikhoan.getEmail());
        return ApiResponse.success(taikhoan, "Khoá người dùng thành công");
    }

    @PutMapping("/unban-user/{matk}")
    public ApiResponse<Taikhoan> unBanUser(@PathVariable String matk) {
        Taikhoan taikhoan = taikhoanquantriService.unBanUser(matk);
        return ApiResponse.success(taikhoan, "Mở khoá người dùng thành công");
    }
}
