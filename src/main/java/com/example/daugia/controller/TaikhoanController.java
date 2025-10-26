package com.example.daugia.controller;

import com.example.daugia.dto.request.ApiResponse;
import com.example.daugia.dto.request.TaikhoanCreationRequest;
import com.example.daugia.entity.Taikhoan;
import com.example.daugia.service.TaikhoanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class TaikhoanController {
    @Autowired
    private TaikhoanService taikhoanService;

    @PostMapping("/create")
    public ApiResponse<Taikhoan> createUser(@RequestBody TaikhoanCreationRequest request){
        ApiResponse<Taikhoan> apiResponse = new ApiResponse<>();
        try {
            apiResponse.setResult(taikhoanService.createUser(request));
            apiResponse.setCode(200);
            apiResponse.setMessage("Tao tai khoan thanh cong");
        } catch (IllegalArgumentException e) {
            apiResponse.setCode(400); // Mã lỗi nếu tên người dùng đã tồn tại
            apiResponse.setMessage(e.getMessage());
        }
        return apiResponse;
    }

    @GetMapping("/find-all")
    public ApiResponse<List<Taikhoan>> findAll(){
        ApiResponse<List<Taikhoan>> apiResponse = new ApiResponse<>();
        try{
            List<Taikhoan> taikhoanList = taikhoanService.findAll();
            apiResponse.setCode(200);
            apiResponse.setMessage("Thanh cong");
            apiResponse.setResult(taikhoanList);
        } catch (IllegalArgumentException e) {
            apiResponse.setCode(500);
            apiResponse.setMessage("That bai:" + e.getMessage());
        }
        return apiResponse;
    }
}
