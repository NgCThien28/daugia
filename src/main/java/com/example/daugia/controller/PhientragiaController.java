package com.example.daugia.controller;

import com.example.daugia.dto.request.ApiResponse;
import com.example.daugia.dto.response.BiddingDTO;
import com.example.daugia.entity.Phientragia;
import com.example.daugia.service.PhientragiaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/biddings")
public class PhientragiaController {
    @Autowired
    private PhientragiaService phientragiaService;

    @GetMapping("/find-all")
    public ApiResponse<List<BiddingDTO>> findAll(){
        ApiResponse<List<BiddingDTO>> apiResponse = new ApiResponse<>();
        try{
            List<BiddingDTO> phientragiaList = phientragiaService.findAll();
            apiResponse.setCode(200);
            apiResponse.setMessage("Thanh cong");
            apiResponse.setResult(phientragiaList);
        } catch (IllegalArgumentException e) {
            apiResponse.setCode(500);
            apiResponse.setMessage("That bai:" + e.getMessage());
        }
        return apiResponse;
    }
}
