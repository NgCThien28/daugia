package com.example.daugia.core.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum KetQuaPhien {
    PENDING_APPROVAL("Chờ duyệt"),
    APPROVED("Đã duyệt"),
    CANCELLED("Đã huỷ");

    private final String value;

    KetQuaPhien(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
