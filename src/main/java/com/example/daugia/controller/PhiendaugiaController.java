package com.example.daugia.controller;

import com.example.daugia.dto.request.ApiResponse;
import com.example.daugia.dto.response.AuctionDTO;
import com.example.daugia.entity.Phiendaugia;
import com.example.daugia.service.PhiendaugiaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/auctions")
public class PhiendaugiaController {
    @Autowired
    private PhiendaugiaService phiendaugiaService;

    @GetMapping("/find-all")
    public ApiResponse<List<AuctionDTO>> findAll(){
        ApiResponse<List<AuctionDTO>> apiResponse = new ApiResponse<>();
        try{
            List<AuctionDTO> phiendaugiaList = phiendaugiaService.findAll();
            apiResponse.setCode(200);
            apiResponse.setMessage("Thanh cong");
            apiResponse.setResult(phiendaugiaList);
        } catch (IllegalArgumentException e) {
            apiResponse.setCode(500);
            apiResponse.setMessage("That bai:" + e.getMessage());
        }
        return apiResponse;
    }
}
