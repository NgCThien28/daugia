package com.example.daugia.controller;

import com.example.daugia.dto.request.ApiResponse;
import com.example.daugia.entity.Hinhanh;
import com.example.daugia.service.HinhanhService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/images")
public class HinhanhController {
    @Autowired
    private HinhanhService hinhanhService;

    @GetMapping("/find-all")
    public ApiResponse<List<Hinhanh>> findAll(){
        ApiResponse<List<Hinhanh>> apiResponse = new ApiResponse<>();
        try{
            List<Hinhanh> hinhanhList = hinhanhService.findAll();
            apiResponse.setCode(200);
            apiResponse.setMessage("Thanh cong");
            apiResponse.setResult(hinhanhList);
        } catch (IllegalArgumentException e) {
            apiResponse.setCode(500);
            apiResponse.setMessage("That bai:" + e.getMessage());
        }
        return apiResponse;
    }
}
