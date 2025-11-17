package com.example.daugia.controller;

import com.example.daugia.dto.request.ApiResponse;
import com.example.daugia.entity.Hinhanh;
import com.example.daugia.service.HinhanhService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/images")
public class HinhanhController {
    @Autowired
    private HinhanhService hinhanhService;

    @GetMapping("/find-all")
    public ApiResponse<List<Hinhanh>> findAll(){
        List<Hinhanh> list = hinhanhService.findAll();
        return ApiResponse.success(list, "Thành công");
    }

    @PostMapping("/upload")
    public ApiResponse<List<Hinhanh>> uploadImages(
            @RequestParam("masp") String masp,
            @RequestParam("files") List<MultipartFile> files) throws IOException {
        List<Hinhanh> saved = hinhanhService.saveFiles(masp, files);
        return ApiResponse.success(saved, "Upload ảnh thành công");
    }

    @PutMapping("/update")
    public ApiResponse<List<Hinhanh>> updateImages(
            @RequestParam("masp") String masp,
            @RequestParam("files") List<MultipartFile> files) throws IOException {
        List<Hinhanh> saved = hinhanhService.updateFiles(masp, files);
        return ApiResponse.success(saved, "Cập nhật ảnh thành công");
    }
}
