package com.example.daugia.controller;

import com.example.daugia.dto.request.ApiResponse;
import com.example.daugia.dto.request.HinhanhCreationRequest;
import com.example.daugia.entity.Hinhanh;
import com.example.daugia.service.HinhanhService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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

    @PostMapping("/upload")
    public ApiResponse<List<Hinhanh>> uploadImages(
            @RequestParam("masp") String masp,
            @RequestParam("files") List<MultipartFile> files) {
        ApiResponse<List<Hinhanh>> response = new ApiResponse<>();
        try {
            List<Hinhanh> saved = hinhanhService.saveFiles(masp, files);
            response.setCode(200);
            response.setMessage("Upload ảnh thành công");
            response.setResult(saved);
        } catch (Exception e) {
            response.setCode(500);
            response.setMessage("Upload thất bại: " + e.getMessage());
        }
        return response;
    }

}
