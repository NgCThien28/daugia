package com.example.daugia.controller;

import com.example.daugia.core.custom.TokenValidator;
import com.example.daugia.core.enums.TrangThaiSanPham;
import com.example.daugia.dto.request.ApiResponse;
import com.example.daugia.dto.request.SanPhamCreationRequest;
import com.example.daugia.dto.response.ProductDTO;
import com.example.daugia.entity.Sanpham;
import com.example.daugia.service.SanphamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/products")
@CrossOrigin("http://localhost:5173/")
public class SanphamController {
    @Autowired
    private SanphamService sanphamService;
    @Autowired
    private TokenValidator tokenValidator;

    @GetMapping("/find-all")
    public ApiResponse<List<ProductDTO>> findAll() {
        List<ProductDTO> list = sanphamService.findAll();
        return ApiResponse.success(list, "Thành công");
    }

    @GetMapping("/find-by-user")
    public ApiResponse<Page<Sanpham>> findByUser(
            @RequestHeader("Authorization") String header,
            @RequestParam(name = "status", required = false) List<TrangThaiSanPham> statuses,
            @PageableDefault(size = 8, sort = "masp", direction = Sort.Direction.ASC)
            Pageable pageable
    ) {
        String email = tokenValidator.authenticateAndGetEmail(header);
        Page<Sanpham> page = (statuses == null || statuses.isEmpty())
                ? sanphamService.findByUser(email, pageable)
                : sanphamService.findByUserWithStatuses(email, statuses, pageable);
        return ApiResponse.success(page, "Thành công");
    }

    @PostMapping("/create")
    public ApiResponse<ProductDTO> create(@RequestBody SanPhamCreationRequest request,
                                          @RequestHeader("Authorization") String header) {
        String email = tokenValidator.authenticateAndGetEmail(header);
        ProductDTO dto = sanphamService.create(request, email);
        return ApiResponse.success(dto, "Tạo sản phẩm thành công");
    }

    @PutMapping("/update")
    public ApiResponse<ProductDTO> update(@RequestBody SanPhamCreationRequest request,
                                          @RequestHeader("Authorization") String header) {
        String email = tokenValidator.authenticateAndGetEmail(header);
        ProductDTO updated = sanphamService.update(request, email);
        return ApiResponse.success(updated, "Cập nhật sản phẩm thành công");
    }

    @DeleteMapping("/delete/{masp}")
    public ApiResponse<String> delete(@PathVariable String masp,
                                      @RequestHeader("Authorization") String header) {
        String email = tokenValidator.authenticateAndGetEmail(header);
        String text = sanphamService.delete(masp, email);
        return ApiResponse.success(null, text);
    }

    @PutMapping("/approve/{masp}")
    public ApiResponse<ProductDTO> approveProduct(
            @PathVariable String masp,
            @RequestHeader("Authorization") String header) {
        String email = tokenValidator.authenticateAndGetEmail(header);
        ProductDTO approved = sanphamService.approveProduct(masp, email);
        return ApiResponse.success(approved, "Duyệt sản phẩm thành công");
    }

    @PutMapping("/reject/{masp}")
    public ApiResponse<ProductDTO> rejectProduct(
            @PathVariable String masp,
            @RequestHeader("Authorization") String header) {
        String email = tokenValidator.authenticateAndGetEmail(header);
        ProductDTO rejected = sanphamService.rejectProduct(masp, email);
        return ApiResponse.success(rejected, "Từ chối sản phẩm thành công");
    }

}
