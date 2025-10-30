package com.example.daugia.core.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum TrangThaiPhieuThanhToanTienCoc {
    UNPAID("Chưa thanh toán"),
    PAID("Đã thanh toán"),
    REFUND("Hoàn trả");

    private final String value;

    TrangThaiPhieuThanhToanTienCoc(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
