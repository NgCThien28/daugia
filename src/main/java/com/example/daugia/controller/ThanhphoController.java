package com.example.daugia.controller;

import com.example.daugia.dto.request.ApiResponse;
import com.example.daugia.entity.Thanhpho;
import com.example.daugia.service.ThanhphoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/cities")
public class ThanhphoController {
    @Autowired
    private ThanhphoService thanhphoService;

    @GetMapping("/find-all")
    public ApiResponse<List<Thanhpho>> findAll(){
        ApiResponse<List<Thanhpho>> apiResponse = new ApiResponse<>();
        try {
            List<Thanhpho> thanhphoList = thanhphoService.findAll();
            apiResponse.setCode(200);
            apiResponse.setResult(thanhphoList);
            apiResponse.setMessage("Thanh cong");
        } catch (IllegalArgumentException e) {
            apiResponse.setCode(500);
            apiResponse.setMessage("That bai:" + e.getMessage());
        }
        return apiResponse;
    }
}
