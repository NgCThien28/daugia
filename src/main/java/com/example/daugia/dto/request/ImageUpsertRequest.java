package com.example.daugia.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Dung cho endpoint upsert hon hop (multipart):
 * Phan JSON (field meta) se chua đoi tuong nay.
 * Phan file (field files) chua danh sach file theo thu tu xuat hien cua cac operation kieu APPEND/REPLACE.
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