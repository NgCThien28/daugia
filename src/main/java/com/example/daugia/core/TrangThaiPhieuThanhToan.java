package com.example.daugia.core;

public enum TrangThaiPhieuThanhToan {
    PENDING_APPROVAL("Chờ duyệt"),
    APPROVED("Đã duyệt"),
    CANCELLED("Đã huỷ");

    private final String value;

    TrangThaiPhieuThanhToan(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
