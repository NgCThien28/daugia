package com.example.daugia.controller;

import com.example.daugia.dto.request.ApiResponse;
import com.example.daugia.entity.Baocao;
import com.example.daugia.service.BaocaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
