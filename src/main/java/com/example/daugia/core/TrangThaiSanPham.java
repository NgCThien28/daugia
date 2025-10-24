package com.example.daugia.core;

public enum TrangThaiSanPham {
    PENDING_APPROVAL("Chờ duyệt"),
    APPROVED("Đã duyệt"),
    CANCELLED("Đã huỷ");

    private final String value;

    TrangThaiSanPham(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
