package com.example.daugia.controller;

import com.example.daugia.dto.request.ApiResponse;
import com.example.daugia.dto.response.NotificationDTO;
import com.example.daugia.entity.Thongbao;
import com.example.daugia.service.ThongbaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/notifications")
public class ThongbaoController {
    @Autowired
    private ThongbaoService thongbaoService;

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
}
