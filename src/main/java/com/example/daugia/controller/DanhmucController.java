package com.example.daugia.controller;

import com.example.daugia.dto.request.ApiResponse;
import com.example.daugia.entity.Danhmuc;
import com.example.daugia.service.DanhmucService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/cates")
public class DanhmucController {
    @Autowired
    private DanhmucService danhmucService;

    @GetMapping("/find-all")
    public ApiResponse<List<Danhmuc>> findAll() {
        List<Danhmuc> danhmucList = danhmucService.findAll();
        return ApiResponse.success(danhmucList, "Thành công");
    }
}
