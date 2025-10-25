package com.example.daugia.controller;

import com.example.daugia.dto.request.ApiResponse;
import com.example.daugia.entity.Taikhoan;
import com.example.daugia.service.TaikhoanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/users")
public class TaikhoanController {
    @Autowired
    private TaikhoanService taikhoanService;

    @GetMapping
    public ApiResponse<List<Taikhoan>> getAll(){
        ApiResponse<List<Taikhoan>> apiResponse = new ApiResponse<>();
        try{
            List<Taikhoan> taikhoanList = taikhoanService.getAllTaiKhoan();
            apiResponse.setCode(200);
            apiResponse.setMessage("Thanh cong");
            apiResponse.setResult(taikhoanList);
        } catch (Exception e) {
            apiResponse.setCode(500);
            apiResponse.setMessage("Khong thanh cong");
        }
        return apiResponse;
    }
}
