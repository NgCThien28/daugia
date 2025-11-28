package com.example.daugia.core.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum TrangThaiThongBao {
    UNSENT("Chưa gửi"),
    NOT_VIEWED("Chưa xem"),
    VIEWED("Đã xem"),
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
