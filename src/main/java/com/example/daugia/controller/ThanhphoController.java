package com.example.daugia.controller;

import com.example.daugia.dto.request.ApiResponse;
import com.example.daugia.dto.request.ThanhPhoRequest;
import com.example.daugia.entity.Thanhpho;
import com.example.daugia.service.ThanhphoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cities")
public class ThanhphoController {
    @Autowired
    private ThanhphoService thanhphoService;

    @GetMapping("/find-all")
    public ApiResponse<List<Thanhpho>> findAll() {
        List<Thanhpho> list = thanhphoService.findAll();
        return ApiResponse.success(list, "Thành công");
    }

    //Admin
    @PostMapping
    public ApiResponse<Thanhpho> create(@RequestBody ThanhPhoRequest request) {
        Thanhpho thanhpho = thanhphoService.create(request.getTentp());
        return ApiResponse.success(thanhpho, "Thêm thành phố thành công");
    }

    //Admin
    @PutMapping("/{matp}")
    public ApiResponse<Thanhpho> update(@PathVariable String matp, @RequestBody ThanhPhoRequest request) {
        Thanhpho thanhpho = thanhphoService.update(matp, request.getTentp());
        return ApiResponse.success(thanhpho, "Cập nhật thành phố thành công");
    }

    //Admin
    @DeleteMapping("/{matp}")
    public ApiResponse<Thanhpho> delete(@PathVariable String matp) {
        thanhphoService.delete(matp);
        return ApiResponse.success(null, "Xoá thành phố thành công");
    }
}
