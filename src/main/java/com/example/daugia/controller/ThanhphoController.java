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
    public ApiResponse<List<Thanhpho>> findAll() {
        List<Thanhpho> list = thanhphoService.findAll();
        return ApiResponse.success(list, "Thành công");
    }
}
