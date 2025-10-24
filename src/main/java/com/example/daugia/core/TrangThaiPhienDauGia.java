package com.example.daugia.core;

public enum TrangThaiPhienDauGia {
    PENDING_APPROVAL("Chờ duyệt"),
    APPROVED("Đã duyệt"),
    CANCELLED("Đã huỷ");

    private final String value;

    TrangThaiPhienDauGia(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
