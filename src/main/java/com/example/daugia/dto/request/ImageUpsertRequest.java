package com.example.daugia.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Dùng cho endpoint upsert hỗn hợp (multipart):
 * Phần JSON (field meta) sẽ chứa đối tượng này.
 * Phần file (field files) chứa danh sách file theo thứ tự xuất hiện của các operation kiểu APPEND/REPLACE.
 */
@Setter
@Getter
public class ImageUpsertRequest {
    private String masp;
    private List<ImageOperation> operations;
    // reorderIndices
    // Ví dụ: [2,0,1] nghĩa là ảnh hiện tại thứ tự cũ [0,1,2] -> mới sẽ thành [2,0,1]
    private List<Integer> reorderIndices;

    public ImageUpsertRequest() {}

}