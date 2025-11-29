package com.example.daugia.core.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum TrangThaiPhieuThanhToanTienCoc {
    UNPAID("Chưa thanh toán"),
    PAID("Đã thanh toán"),
    REFUNDING("Đang hoàn tiền"),
    REFUNDED("Đã hoàn tiền"),
    CANCELLED("Bị hủy");
    private final String value;

    TrangThaiPhieuThanhToanTienCoc(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
