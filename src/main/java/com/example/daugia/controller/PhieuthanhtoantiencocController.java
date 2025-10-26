package com.example.daugia.controller;

import com.example.daugia.dto.request.ApiResponse;
import com.example.daugia.dto.response.DepositDTO;
import com.example.daugia.service.PhieuthanhtoantiencocService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/deposit-payments")
public class PhieuthanhtoantiencocController {
    @Autowired
    private PhieuthanhtoantiencocService phieuthanhtoantiencocService;

    @GetMapping("/find-all")
    public ApiResponse<List<DepositDTO>> findAll(){
        ApiResponse<List<DepositDTO>> apiResponse = new ApiResponse<>();
        try {
            List<DepositDTO> depositDTOList = phieuthanhtoantiencocService.findAll();
            apiResponse.setCode(200);
            apiResponse.setMessage("thanh cong");
            apiResponse.setResult(depositDTOList);
        } catch (IllegalArgumentException e) {
            apiResponse.setCode(500);
            apiResponse.setMessage("That bai:" + e.getMessage());
        }
        return apiResponse;
    }
}
