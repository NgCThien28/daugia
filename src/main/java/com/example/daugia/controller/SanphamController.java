package com.example.daugia.controller;

import com.example.daugia.core.custom.TokenValidator;
import com.example.daugia.core.enums.TrangThaiSanPham;
import com.example.daugia.dto.request.ApiResponse;
import com.example.daugia.dto.request.SanPhamCreationRequest;
import com.example.daugia.dto.request.ThongBaoCreationRequest;
import com.example.daugia.dto.response.ProductDTO;
import com.example.daugia.entity.Sanpham;
import com.example.daugia.service.SanphamService;
import com.example.daugia.service.ThongbaoService;
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
    @Autowired
    private ThongbaoService thongbaoService;

    @GetMapping("/find-all")
    public ApiResponse<Page<ProductDTO>> findAll(
            @RequestParam(required = false) TrangThaiSanPham trangthai,
            @PageableDefault(size = 12, sort = "masp", direction = Sort.Direction.ASC)
            Pageable pageable
    ) {
        Page<ProductDTO> list = sanphamService.findAll(trangthai, pageable);
        return ApiResponse.success(list, "Thành công");
    }

    @GetMapping("/find-by-user")
    public ApiResponse<Page<Sanpham>> findByUser(
            @RequestHeader("Authorization") String header,
            @RequestParam(name = "status", required = false) List<TrangThaiSanPham> statuses,
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 8, sort = "masp", direction = Sort.Direction.ASC)
            Pageable pageable
    ) {
        String email = tokenValidator.authenticateAndGetEmail(header);
        Page<Sanpham> page = sanphamService.findByUser(email, statuses, keyword, pageable);
        return ApiResponse.success(page, "Thành công");
    }

    @PostMapping("/create")
    public ApiResponse<ProductDTO> create(@RequestBody SanPhamCreationRequest request,
                                          @RequestHeader("Authorization") String header) {
        String email = tokenValidator.authenticateAndGetEmail(header);
        ProductDTO dto = sanphamService.create(request, email);
        return ApiResponse.success(dto, "Tạo tài sản thành công");
    }

    @PutMapping("/register")
    public ApiResponse<ProductDTO> register(@RequestParam(name = "masp") String masp,
                                            @RequestHeader("Authorization") String header){
        String email = tokenValidator.authenticateAndGetEmail(header);
        ProductDTO dto = sanphamService.register(masp,email);
        return ApiResponse.success(dto, "Đăng ký tài sản thành công");
    }

    @PutMapping("/update")
    public ApiResponse<ProductDTO> update(@RequestBody SanPhamCreationRequest request,
                                          @RequestHeader("Authorization") String header) {
        String email = tokenValidator.authenticateAndGetEmail(header);
        ProductDTO updated = sanphamService.update(request, email);
        return ApiResponse.success(updated, "Cập nhật tài sản thành công");
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
            @RequestBody SanPhamCreationRequest request,
            @PathVariable String masp,
            @RequestHeader("Authorization") String header) {
        String email = tokenValidator.authenticateAndGetEmail(header);
        ProductDTO approved = sanphamService.approveProduct(request, masp, email);
        thongbaoService.createForProductToUser(approved.getMasp(), email, approved.getTaiKhoanNguoiBan().getEmail());
        return ApiResponse.success(approved, "Duyệt sản phẩm thành công");
    }

    @PutMapping("/reject/{masp}")
    public ApiResponse<ProductDTO> rejectProduct(
            @RequestBody ThongBaoCreationRequest request,
            @PathVariable String masp,
            @RequestHeader("Authorization") String header) {
        String email = tokenValidator.authenticateAndGetEmail(header);
        ProductDTO rejected = sanphamService.rejectProduct(masp, email);
        thongbaoService.createForUser(request, email, rejected.getTaiKhoanNguoiBan().getEmail());
        return ApiResponse.success(rejected, "Từ chối tài sản thành công");
    }

}
