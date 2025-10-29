package com.example.daugia.core.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum TrangThaiThongBao {
    UNSENT("Chưa gửi"),
    SEND("Đã gửi"),
    CANCELED("Đã huỷ");

    private final String value;

    TrangThaiThongBao(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
