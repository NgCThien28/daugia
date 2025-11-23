package com.example.daugia.core.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum TrangThaiPhieuThanhToan {
    UNPAID("Chưa thanh toán"),
    PAID("Đã thanh toán");

    private final String value;

    TrangThaiPhieuThanhToan(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
