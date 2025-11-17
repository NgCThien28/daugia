package com.example.daugia.controller;

import com.example.daugia.core.custom.TokenValidator;
import com.example.daugia.dto.request.ApiResponse;
import com.example.daugia.dto.request.BaoCaoCreationRequest;
import com.example.daugia.entity.Baocao;
import com.example.daugia.service.ActiveTokenService;
import com.example.daugia.service.BaocaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("reports")
public class BaocaoController {
    @Autowired
    private BaocaoService baocaoService;
    @Autowired
    private ActiveTokenService activeTokenService;
    @Autowired
    private TokenValidator tokenValidator;

    @GetMapping("/find-all")
    public ApiResponse<List<Baocao>> findAll() {
        List<Baocao> list = baocaoService.findAll();
        return ApiResponse.success(list, "Thành công");
    }

    @PostMapping("/create")
    public ApiResponse<Baocao> create(@RequestBody BaoCaoCreationRequest request,
                                      @RequestHeader("Authorization") String header) {
        String email = tokenValidator.authenticateAndGetEmail(header);
        Baocao saved = baocaoService.create(request, email);
        return ApiResponse.success(saved, "Thành công");
    }

    @PutMapping("/update/{mabc}")
    public ApiResponse<Baocao> update(@PathVariable String mabc,
                                      @RequestBody BaoCaoCreationRequest request,
                                      @RequestHeader("Authorization") String header) {
        String email = tokenValidator.authenticateAndGetEmail(header);
        Baocao updated = baocaoService.update(mabc, request, email);
        return ApiResponse.success(updated, "Cập nhật thành công");
    }

    @DeleteMapping("/delete/{mabc}")
    public ApiResponse<Void> delete(@PathVariable String mabc) {
        baocaoService.delete(mabc);
        return ApiResponse.success(null, "Xoá thành công");
    }
}
