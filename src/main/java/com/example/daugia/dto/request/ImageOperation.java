package com.example.daugia.dto.request;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ImageOperation {

    public enum OpType {
        APPEND,
        REPLACE,
        REMOVE,
        REORDER
    }

    private OpType type;
    // index được dùng cho REPLACE hoặc REMOVE (0-based)
    private Integer index;
    // tên phần file trong multipart
    private String fileKey;

    public ImageOperation() {}

    public ImageOperation(OpType type, Integer index) {
        this.type = type;
        this.index = index;
    }

}