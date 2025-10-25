package com.example.daugia.controller;

import com.example.daugia.dto.request.ApiResponse;
import com.example.daugia.dto.response.ProductDTO;
import com.example.daugia.entity.Sanpham;
import com.example.daugia.service.SanphamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
