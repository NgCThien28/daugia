package com.example.daugia.controller;

import com.example.daugia.core.enums.TrangThaiPhienDauGia;
import com.example.daugia.dto.request.ApiResponse;
import com.example.daugia.dto.request.PhiendaugiaCreationRequest;
import com.example.daugia.dto.response.AuctionDTO;
import com.example.daugia.entity.Phiendaugia;
import com.example.daugia.service.BlacklistService;
import com.example.daugia.service.PhiendaugiaService;
import com.example.daugia.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/auctions")
public class PhiendaugiaController {
    @Autowired
    private PhiendaugiaService phiendaugiaService;
    @Autowired
    private BlacklistService blacklistService;

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

    @GetMapping("/find-by-id/{id}")
    public ApiResponse<AuctionDTO> findById(@PathVariable("id") String id) {
        ApiResponse<AuctionDTO> apiResponse = new ApiResponse<>();
        try{
            AuctionDTO phiendaugia = phiendaugiaService.findById(id);
            apiResponse.setCode(200);
            apiResponse.setMessage("Thanh cong");
            apiResponse.setResult(phiendaugia);
        } catch (IllegalArgumentException e) {
            apiResponse.setCode(500);
            apiResponse.setMessage("That bai:" + e.getMessage());
        }
        return apiResponse;
    }

    @GetMapping("find-by-user")
    public ApiResponse<List<Phiendaugia>> findByUser(@RequestHeader("Authorization") String header) {
        ApiResponse<List<Phiendaugia>> res = new ApiResponse<>();
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
            res.setResult(phiendaugiaService.findByUser(email));
            res.setCode(200);
            res.setMessage("Thanh cong");
        } catch (Exception e) {
            res.setCode(500);
            res.setMessage("Lỗi: " + e.getMessage());
        }
        return res;
    }

    @GetMapping("find-by-status")
    public ApiResponse<List<AuctionDTO>> findByStatus() {
        ApiResponse<List<AuctionDTO>> apiResponse = new ApiResponse<>();
        try{
            List<AuctionDTO> phiendaugiaList = phiendaugiaService.findByStatus(TrangThaiPhienDauGia.APPROVED);
            apiResponse.setCode(200);
            apiResponse.setMessage("Thanh cong");
            apiResponse.setResult(phiendaugiaList);
        } catch (IllegalArgumentException e) {
            apiResponse.setCode(500);
            apiResponse.setMessage("That bai:" + e.getMessage());
        }
        return apiResponse;
    }

    @PostMapping("/create")
    public ApiResponse<AuctionDTO> create(@RequestBody PhiendaugiaCreationRequest request,
                                          @RequestHeader("Authorization") String header) {
        ApiResponse<AuctionDTO> res = new ApiResponse<>();
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
            res.setResult(phiendaugiaService.create(request,email));
            res.setCode(200);
            res.setMessage("Tạo thành công");
        } catch (Exception e) {
            res.setCode(500);
            res.setMessage("Lỗi: " + e.getMessage());
        }
        return res;
    }

}
