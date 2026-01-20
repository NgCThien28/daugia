package com.example.daugia.core.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum TrangThaiTaiKhoan {
    ONLINE("Đang hoạt động"),
    OFFLINE("Ngoại tuyến"),
    INACTIVE("Chưa xác thực"),
    ACTIVE("Đã xác thực"),
    BANNED("Đã bị khoá");

    private final String value;

    TrangThaiTaiKhoan(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
