package com.example.daugia.controller;

import com.example.daugia.dto.request.ApiResponse;
import com.example.daugia.dto.request.ImageUpsertRequest;
import com.example.daugia.entity.Hinhanh;
import com.example.daugia.exception.NotFoundException;
import com.example.daugia.exception.StorageException;
import com.example.daugia.exception.ValidationException;
import com.example.daugia.service.HinhanhService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@RestController
@RequestMapping("/images")
public class HinhanhController {

    @Autowired
    private HinhanhService hinhanhService;
    @Autowired
    private ObjectMapper objectMapper;

    @GetMapping("/find-all")
    public ApiResponse<List<Hinhanh>> findAll() {
        List<Hinhanh> list = hinhanhService.findAll();
        return ApiResponse.success(list, "Thành công");
    }

    /* Khởi tạo ảnh ban đầu (tối đa 3) */
    @PostMapping(value = "/init", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<List<Hinhanh>> initImages(
            @RequestParam("masp") String masp,
            @RequestParam("files") MultipartFile[] files) {
        try {
            List<MultipartFile> list = Arrays.asList(files);
            List<Hinhanh> saved = hinhanhService.createInitial(masp, list);
            return ApiResponse.success(saved, "Tạo mới ảnh thành công");
        } catch (ValidationException e) {
            return ApiResponse.error(400, e.getMessage());
        } catch (NotFoundException e) {
            return ApiResponse.error(404, e.getMessage());
        } catch (StorageException e) {
            return ApiResponse.error(500, e.getMessage());
        } catch (Exception e) {
            return ApiResponse.error(500, "Lỗi không xác định: " + e.getMessage());
        }
    }

    /* Append ảnh (chỉ thêm nếu chưa đủ 3) */
    @PostMapping(value = "/append", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<List<Hinhanh>> appendImages(
            @RequestParam("masp") String masp,
            @RequestParam("files") MultipartFile[] files) {
        try {
            List<Hinhanh> saved = hinhanhService.append(masp, Arrays.asList(files));
            return ApiResponse.success(saved, "Thêm ảnh thành công");
        } catch (ValidationException e) {
            return ApiResponse.error(400, e.getMessage());
        } catch (NotFoundException e) {
            return ApiResponse.error(404, e.getMessage());
        } catch (StorageException e) {
            return ApiResponse.error(500, e.getMessage());
        } catch (Exception e) {
            return ApiResponse.error(500, "Lỗi không xác định: " + e.getMessage());
        }
    }

    /* Replace chỉ số cụ thể (partial): indices[] đi kèm files[] */
    @PutMapping(value = "/replace-indices", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<List<Hinhanh>> replaceIndices(
            @RequestParam("masp") String masp,
            @RequestParam("files") MultipartFile[] files,
            @RequestParam("indices") List<Integer> indices) {
        try {
            if (files.length != indices.size()) {
                return ApiResponse.error(400, "Số file không khớp số index");
            }
            Map<Integer, MultipartFile> replaceMap = new HashMap<>();
            for (int i = 0; i < indices.size(); i++) {
                replaceMap.put(indices.get(i), files[i]);
            }
            return ApiResponse.success(hinhanhService.replaceIndices(masp, replaceMap), "Thay ảnh theo index thành công");
        } catch (ValidationException e) {
            return ApiResponse.error(400, e.getMessage());
        } catch (NotFoundException e) {
            return ApiResponse.error(404, e.getMessage());
        } catch (StorageException e) {
            return ApiResponse.error(500, e.getMessage());
        } catch (Exception e) {
            return ApiResponse.error(500, "Lỗi không xác định: " + e.getMessage());
        }
    }

    /* Replace toàn bộ */
    @PutMapping(value = "/replace-all", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<List<Hinhanh>> replaceAll(
            @RequestParam("masp") String masp,
            @RequestParam("files") MultipartFile[] files) {
        try {
            return ApiResponse.success(hinhanhService.replaceAll(masp, Arrays.asList(files)), "Thay toàn bộ ảnh thành công");
        } catch (ValidationException e) {
            return ApiResponse.error(400, e.getMessage());
        } catch (NotFoundException e) {
            return ApiResponse.error(404, e.getMessage());
        } catch (StorageException e) {
            return ApiResponse.error(500, e.getMessage());
        } catch (Exception e) {
            return ApiResponse.error(500, "Lỗi không xác định: " + e.getMessage());
        }
    }

    /* Remove theo index (nhiều) */
    @DeleteMapping("/remove-indices")
    public ApiResponse<List<Hinhanh>> removeIndices(@RequestParam("masp") String masp,
                                                    @RequestParam("indices") List<Integer> indices) {
        try {
            return ApiResponse.success(hinhanhService.removeByIndices(masp, indices), "Xóa ảnh theo index thành công");
        } catch (ValidationException e) {
            return ApiResponse.error(400, e.getMessage());
        } catch (NotFoundException e) {
            return ApiResponse.error(404, e.getMessage());
        } catch (Exception e) {
            return ApiResponse.error(500, "Lỗi không xác định: " + e.getMessage());
        }
    }

    /* Remove theo id ảnh (nhiều) */
    @DeleteMapping("/remove-ids")
    public ApiResponse<List<Hinhanh>> removeIds(@RequestParam("masp") String masp,
                                                @RequestParam("imageIds") List<String> imageIds) {
        try {
            return ApiResponse.success(hinhanhService.removeByIds(masp, imageIds), "Xóa ảnh theo id thành công");
        } catch (ValidationException e) {
            return ApiResponse.error(400, e.getMessage());
        } catch (NotFoundException e) {
            return ApiResponse.error(404, e.getMessage());
        } catch (Exception e) {
            return ApiResponse.error(500, "Lỗi không xác định: " + e.getMessage());
        }
    }

    /* Reorder ảnh */
    @PatchMapping("/reorder")
    public ApiResponse<List<Hinhanh>> reorder(@RequestParam("masp") String masp,
                                              @RequestParam("order") List<Integer> order) {
        try {
            return ApiResponse.success(hinhanhService.reorder(masp, order), "Sắp xếp lại ảnh thành công");
        } catch (ValidationException e) {
            return ApiResponse.error(400, e.getMessage());
        } catch (NotFoundException e) {
            return ApiResponse.error(404, e.getMessage());
        } catch (Exception e) {
            return ApiResponse.error(500, "Lỗi không xác định: " + e.getMessage());
        }
    }

    /* Upsert hỗn hợp: multipart gồm meta + files */
    @PostMapping(value = "/upsert", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<?> upsertMixed(
            @RequestPart("meta") String metaJson,
            @RequestPart(value = "files", required = false) List<MultipartFile> files) {

        try {
            ImageUpsertRequest meta = objectMapper.readValue(metaJson, ImageUpsertRequest.class);
            List<MultipartFile> safeFiles = files == null ? Collections.emptyList() : files;
            List<Hinhanh> result = hinhanhService.upsertMixed(meta, safeFiles);
            return ApiResponse.success(result, "Upsert ảnh thành công");
        } catch (ValidationException e) {
            return ApiResponse.error(400, e.getMessage());
        } catch (NotFoundException e) {
            return ApiResponse.error(404, e.getMessage());
        } catch (StorageException e) {
            return ApiResponse.error(500, e.getMessage());
        } catch (Exception e) {
            return ApiResponse.error(500, "Lỗi parse hoặc xử lý: " + e.getMessage());
        }
    }
}