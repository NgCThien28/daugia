package com.example.daugia.controller;

import com.example.daugia.dto.request.ApiResponse;
import com.example.daugia.entity.Taikhoanquantri;
import com.example.daugia.service.TaikhoanquantriService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admins")
public class TaikhoanquantriController {
    @Autowired
    private TaikhoanquantriService taikhoanquantriService;

    @GetMapping("/find-all")
    public ApiResponse<List<Taikhoanquantri>> findAll() {
        List<Taikhoanquantri> list = taikhoanquantriService.findAll();
        return ApiResponse.success(list, "Thành công");
    }
}
