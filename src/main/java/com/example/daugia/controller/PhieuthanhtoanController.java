package com.example.daugia.controller;

import com.example.daugia.dto.request.ApiResponse;
import com.example.daugia.dto.response.PaymentDTO;
import com.example.daugia.entity.Phieuthanhtoan;
import com.example.daugia.service.PhieuthanhtoanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/payments")
public class PhieuthanhtoanController {
    @Autowired
    private PhieuthanhtoanService phieuthanhtoanService;

    @GetMapping("/find-all")
    public ApiResponse<List<PaymentDTO>> findAll(){
        ApiResponse<List<PaymentDTO>> apiResponse = new ApiResponse<>();
        try {
            List<PaymentDTO> phieuthanhtoanList = phieuthanhtoanService.findAll();
            apiResponse.setCode(200);
            apiResponse.setMessage("thanh cong");
            apiResponse.setResult(phieuthanhtoanList);
        } catch (IllegalArgumentException e) {
            apiResponse.setCode(500);
            apiResponse.setMessage("That bai:" + e.getMessage());
        }
        return apiResponse;
    }
}
