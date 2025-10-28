package com.example.daugia.controller;

import com.example.daugia.dto.request.ApiResponse;
import com.example.daugia.dto.request.BaoCaoCreationRequest;
import com.example.daugia.entity.Baocao;
import com.example.daugia.service.BaocaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("reports")
public class BaocaoController {
    @Autowired
    private BaocaoService baocaoService;

    @GetMapping("/find-all")
    public ApiResponse<List<Baocao>> findAll() {
        ApiResponse<List<Baocao>> apiResponse = new ApiResponse<>();
        try{
            List<Baocao> baocaoList = baocaoService.findAll();
            apiResponse.setCode(200);
            apiResponse.setMessage("Thanh cong");
            apiResponse.setResult(baocaoList);
        } catch (IllegalArgumentException e) {
            apiResponse.setCode(500);
            apiResponse.setMessage("That bai:" + e.getMessage());
        }
        return apiResponse;
    }

    @PostMapping("/create")
    public ApiResponse<Baocao> create(@RequestBody BaoCaoCreationRequest request) {
        ApiResponse<Baocao> apiResponse = new ApiResponse<>();
        try{
            apiResponse.setResult(baocaoService.create(request));
            apiResponse.setCode(200);
            apiResponse.setMessage("Thanh cong");
        } catch (IllegalArgumentException e) {
            apiResponse.setCode(500);
            apiResponse.setMessage("That bai:" + e.getMessage());
        }
        return apiResponse;
    }

    @PutMapping("/update/{mabc}")
    public ApiResponse<Baocao> update(@PathVariable String mabc, @RequestBody BaoCaoCreationRequest request) {
        ApiResponse<Baocao> apiResponse = new ApiResponse<>();
        try {
            apiResponse.setResult(baocaoService.update(mabc, request));
            apiResponse.setCode(200);
            apiResponse.setMessage("Cập nhật thành công");
        } catch (Exception e) {
            apiResponse.setCode(500);
            apiResponse.setMessage("Thất bại: " + e.getMessage());
        }
        return apiResponse;
    }

    @DeleteMapping("/delete/{mabc}")
    public ApiResponse<Void> delete(@PathVariable String mabc) {
        ApiResponse<Void> apiResponse = new ApiResponse<>();
        try {
            baocaoService.delete(mabc);
            apiResponse.setCode(200);
            apiResponse.setMessage("Xoá thành công");
        } catch (Exception e) {
            apiResponse.setCode(500);
            apiResponse.setMessage("Thất bại: " + e.getMessage());
        }
        return apiResponse;
    }
}
