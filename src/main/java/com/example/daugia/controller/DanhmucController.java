package com.example.daugia.controller;

import com.example.daugia.dto.request.ApiResponse;
import com.example.daugia.dto.request.DanhMucRequest;
import com.example.daugia.entity.Danhmuc;
import com.example.daugia.service.DanhmucService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cates")
public class DanhmucController {
    @Autowired
    private DanhmucService danhmucService;

    @GetMapping("/find-all")
    public ApiResponse<List<Danhmuc>> findAll() {
        List<Danhmuc> danhmucList = danhmucService.findAll();
        return ApiResponse.success(danhmucList, "Thành công");
    }

    //Admin
    @PostMapping
    public ApiResponse<Danhmuc> create(@RequestBody DanhMucRequest request) {
        Danhmuc danhmuc = danhmucService.create(request.getTendm());
        return ApiResponse.success(danhmuc, "Thêm danh mục thành công");
    }

    //Admin
    @PutMapping("/{madm}")
    public ApiResponse<Danhmuc> update(@PathVariable String madm, @RequestBody DanhMucRequest request) {
        Danhmuc danhmuc = danhmucService.update(madm, request.getTendm());
        return ApiResponse.success(danhmuc, "Cập nhật danh mục thành công");
    }

    //Admin
    @DeleteMapping("/{madm}")
    public ApiResponse<Danhmuc> delete(@PathVariable String madm) {
        danhmucService.delete(madm);
        return ApiResponse.success(null, "Xoá danh mục thành công");
    }
}
