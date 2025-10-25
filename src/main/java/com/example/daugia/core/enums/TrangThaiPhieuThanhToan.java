package com.example.daugia.core.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum TrangThaiPhieuThanhToan {
    PENDING_APPROVAL("Chờ duyệt"),
    APPROVED("Đã duyệt"),
    CANCELLED("Đã huỷ");

    private final String value;

    TrangThaiPhieuThanhToan(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
