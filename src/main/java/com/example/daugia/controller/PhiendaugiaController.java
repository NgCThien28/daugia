package com.example.daugia.controller;

import com.example.daugia.core.custom.TokenValidator;
import com.example.daugia.core.enums.TrangThaiPhienDauGia;
import com.example.daugia.dto.request.ApiResponse;
import com.example.daugia.dto.request.PhiendaugiaCreationRequest;
import com.example.daugia.dto.response.AuctionDTO;
import com.example.daugia.service.PhiendaugiaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/auctions")
public class PhiendaugiaController {
    @Autowired
    private PhiendaugiaService phiendaugiaService;
    @Autowired
    private TokenValidator tokenValidator;

    @GetMapping("/find-all")
    public ApiResponse<List<AuctionDTO>> findAll() {
        List<AuctionDTO> list = phiendaugiaService.findAllDTO();
        return ApiResponse.success(list, "Thành công");
    }

    @GetMapping("/find-by-id/{id}")
    public ApiResponse<AuctionDTO> findById(@PathVariable String id) {
        AuctionDTO dto = phiendaugiaService.findByIdDTO(id);
        return ApiResponse.success(dto, "Thành công");
    }

    @GetMapping("/find-by-user")
    public ApiResponse<List<AuctionDTO>> findByUser(@RequestHeader("Authorization") String header) {
        String email = tokenValidator.authenticateAndGetEmail(header);
        List<AuctionDTO> list = phiendaugiaService.findByUser(email);
        return ApiResponse.success(list, "Thành công");
    }

    @GetMapping("/find-by-status")
    public ApiResponse<Page<AuctionDTO>> findByStatus(
            @RequestParam(name = "status", required = false) List<TrangThaiPhienDauGia> statuses,
            @PageableDefault(size = 12, sort = "thoigianbd", direction = Sort.Direction.ASC)
            Pageable pageable
    ) {
        if (statuses == null || statuses.isEmpty()) {
            statuses = List.of(
                    TrangThaiPhienDauGia.NOT_STARTED,
                    TrangThaiPhienDauGia.IN_PROGRESS,
                    TrangThaiPhienDauGia.SUCCESS
            );
        }
        Page<AuctionDTO> page = phiendaugiaService.findByStatusesPaged(statuses, pageable);
        return ApiResponse.success(page, "Thành công");
    }

    @GetMapping("/find-by-single-status")
    public ApiResponse<Page<AuctionDTO>> findBySingleStatus(
            @RequestParam("status") TrangThaiPhienDauGia status,
            @RequestParam(value = "startDateFrom", required = false) Long startDateFrom,
            @RequestParam(value = "startDateTo", required = false) Long startDateTo,
            @PageableDefault(size = 12, sort = "thoigianbd", direction = Sort.Direction.ASC)
            Pageable pageable
    ) {
        Page<AuctionDTO> page = phiendaugiaService.findByStatusPagedWithTimeFilter(status, startDateFrom, startDateTo, pageable);
        return ApiResponse.success(page, "Thành công");
    }

    @GetMapping("/find-filtered")
    public ApiResponse<Page<AuctionDTO>> findFilteredAuctions(
            @RequestParam(value = "statuses", required = false) List<TrangThaiPhienDauGia> statuses,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "cateId", required = false) String cateId,
            @RequestParam(value = "regionId", required = false) String regionId,
            @RequestParam(value = "minPrice", required = false) BigDecimal minPrice,
            @RequestParam(value = "maxPrice", required = false) BigDecimal maxPrice,
            @RequestParam(value = "startDateFrom", required = false) Long startDateFrom,
            @RequestParam(value = "startDateTo", required = false) Long startDateTo,
            @PageableDefault(size = 12, sort = "thoigianbd", direction = Sort.Direction.ASC)
            Pageable pageable
    ) {
        Page<AuctionDTO> page = phiendaugiaService.findFilteredAuctions(
                statuses, keyword, cateId, regionId, minPrice, maxPrice, startDateFrom, startDateTo, pageable
        );
        return ApiResponse.success(page, "Thành công");
    }

    @GetMapping("/auction-paid")
    public ApiResponse<Page<AuctionDTO>> getPaidAuctionsByMatk(
            @RequestHeader("Authorization") String header,
            @PageableDefault(size = 12, sort = "thoigianbd", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        String email = tokenValidator.authenticateAndGetEmail(header);
        Page<AuctionDTO> page = phiendaugiaService.getPaidAuctionsByMatk(email, pageable);
        return ApiResponse.success(page, "Thành công");
    }

    @PostMapping("/create")
    public ApiResponse<AuctionDTO> create(@RequestBody PhiendaugiaCreationRequest request,
                                          @RequestHeader("Authorization") String header) {
        String email = tokenValidator.authenticateAndGetEmail(header);
        AuctionDTO dto = phiendaugiaService.create(request, email);
        return ApiResponse.success(dto, "Tạo phiên thành công");
    }
}
