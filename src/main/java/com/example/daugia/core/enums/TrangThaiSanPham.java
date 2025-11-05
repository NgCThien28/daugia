package com.example.daugia.core.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum TrangThaiSanPham {
    PENDING_APPROVAL("Chờ duyệt"),
    APPROVED("Đã duyệt"),
    AUCTION_CREATED("Đã tạo phiên"),
    CANCELLED("Đã huỷ");

    private final String value;

    TrangThaiSanPham(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
