package com.example.daugia.core.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum TrangThaiSanPham {
    NOT_REGISTERED("Chưa đăng ký"),
    PENDING_APPROVAL("Chờ duyệt"),
    APPROVED("Đã duyệt"),
    AUCTION_CREATED("Đã tạo phiên"),
    CANCELLED("Đã hủy");

    private final String value;

    TrangThaiSanPham(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
