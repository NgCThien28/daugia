package com.example.daugia.controller;

import com.example.daugia.dto.request.ApiResponse;
import com.example.daugia.dto.request.SanPhamCreationRequest;
import com.example.daugia.dto.response.ProductDTO;
import com.example.daugia.service.SanphamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/products")
public class SanphamController {
    @Autowired
    private SanphamService sanphamService;

    @GetMapping("/find-all")
    public ApiResponse<List<ProductDTO>> findAll(){
        ApiResponse<List<ProductDTO>> apiResponse = new ApiResponse<>();
        try{
            List<ProductDTO> sanphamList = sanphamService.findAll();
            apiResponse.setCode(200);
            apiResponse.setMessage("Thanh cong");
            apiResponse.setResult(sanphamList);
        } catch (IllegalArgumentException e) {
            apiResponse.setCode(500);
            apiResponse.setMessage("That bai:" + e.getMessage());
        }
        return apiResponse;
    }

    @PostMapping("/create")
    public ApiResponse<ProductDTO> create(@RequestBody SanPhamCreationRequest request) {
        ApiResponse<ProductDTO> res = new ApiResponse<>();
        try {
            res.setResult(sanphamService.create(request));
            res.setCode(200);
            res.setMessage("Tạo thành công");
        } catch (Exception e) {
            res.setCode(500);
            res.setMessage("Lỗi: " + e.getMessage());
        }
        return res;
    }
}
