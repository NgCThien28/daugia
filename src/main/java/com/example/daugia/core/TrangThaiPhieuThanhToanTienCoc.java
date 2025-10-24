package com.example.daugia.core;

public enum TrangThaiPhieuThanhToanTienCoc {
    PENDING_APPROVAL("Chờ duyệt"),
    APPROVED("Đã duyệt"),
    CANCELLED("Đã huỷ");

    private final String value;

    TrangThaiPhieuThanhToanTienCoc(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
