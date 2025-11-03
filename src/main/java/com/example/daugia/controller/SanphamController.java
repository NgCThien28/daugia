package com.example.daugia.controller;

import com.example.daugia.dto.request.ApiResponse;
import com.example.daugia.dto.request.SanPhamCreationRequest;
import com.example.daugia.dto.response.ProductDTO;
import com.example.daugia.entity.Sanpham;
import com.example.daugia.service.BlacklistService;
import com.example.daugia.service.SanphamService;
import com.example.daugia.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/products")
public class SanphamController {
    @Autowired
    private SanphamService sanphamService;
    @Autowired
    private BlacklistService blacklistService;
    @GetMapping("/find-all")
    public ApiResponse<List<ProductDTO>> findAll(){
        ApiResponse<List<ProductDTO>> apiResponse = new ApiResponse<>();
        try{
            List<ProductDTO> sanphamList = sanphamService.findAll();
            apiResponse.setCode(200);
            apiResponse.setMessage("Thanh cong");
            apiResponse.setResult(sanphamList);
        } catch (IllegalArgumentException e) {
            apiResponse.setCode(500);
            apiResponse.setMessage("That bai:" + e.getMessage());
        }
        return apiResponse;
    }

    @GetMapping("/find-by-user")
    public ApiResponse<List<Sanpham>> findByUser(@RequestHeader("Authorization") String header){
        ApiResponse<List<Sanpham>> res = new ApiResponse<>();
        try {
            if (header == null || !header.startsWith("Bearer ")) {
                res.setCode(401);
                res.setMessage("Thiếu token");
                return res;
            }

            String token = header.substring(7);

            // Kiểm tra token có bị vô hiệu hóa không
            if (blacklistService.isBlacklisted(token)) {
                res.setCode(401);
                res.setMessage("Token đã bị vô hiệu hóa");
                return res;
            }

            String email = JwtUtil.validateToken(token);

            if (email == null) {
                res.setCode(401);
                res.setMessage("Token không hợp lệ hoặc hết hạn");
                return res;
            }
            res.setResult(sanphamService.findByUser(email));
            res.setCode(200);
            res.setMessage("Thanh cong");
        } catch (Exception e) {
            res.setCode(500);
            res.setMessage("Lỗi: " + e.getMessage());
        }
        return res;
    }

    @PostMapping("/create")
    public ApiResponse<ProductDTO> create(@RequestBody SanPhamCreationRequest request,
                                          @RequestHeader("Authorization") String header) {
        ApiResponse<ProductDTO> res = new ApiResponse<>();
        try {
            if (header == null || !header.startsWith("Bearer ")) {
                res.setCode(401);
                res.setMessage("Thiếu token");
                return res;
            }

            String token = header.substring(7);

            // Kiểm tra token có bị vô hiệu hóa không
            if (blacklistService.isBlacklisted(token)) {
                res.setCode(401);
                res.setMessage("Token đã bị vô hiệu hóa");
                return res;
            }

            String email = JwtUtil.validateToken(token);

            if (email == null) {
                res.setCode(401);
                res.setMessage("Token không hợp lệ hoặc hết hạn");
                return res;
            }
            res.setResult(sanphamService.create(request, email));
            res.setCode(200);
            res.setMessage("Tạo thành công");
        } catch (Exception e) {
            res.setCode(500);
            res.setMessage("Lỗi: " + e.getMessage());
        }
        return res;
    }

    @PutMapping("/update")
    public ApiResponse<ProductDTO> update(@RequestBody SanPhamCreationRequest request,
                                                 @RequestHeader("Authorization") String header) {
        ApiResponse<ProductDTO> response = new ApiResponse<>();
        try {
            if (header == null || !header.startsWith("Bearer ")) {
                response.setCode(401);
                response.setMessage("Thiếu token");
                return response;
            }

            String token = header.substring(7);
            if (blacklistService.isBlacklisted(token)) {
                response.setCode(401);
                response.setMessage("Token đã bị vô hiệu hóa");
                return response;
            }

            String email = JwtUtil.validateToken(token);
            if (email == null) {
                response.setCode(401);
                response.setMessage("Token không hợp lệ hoặc hết hạn");
                return response;
            }

            ProductDTO updatedProduct = sanphamService.update(request);

            response.setCode(200);
            response.setMessage("Cập nhật sản phẩm thành công");
            response.setResult(updatedProduct);

        } catch (IllegalArgumentException e) {
            response.setCode(400);
            response.setMessage("Lỗi: " + e.getMessage());
        } catch (Exception e) {
            response.setCode(500);
            response.setMessage("Lỗi hệ thống: " + e.getMessage());
        }

        return response;
    }
}
