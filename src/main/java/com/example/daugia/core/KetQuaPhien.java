package com.example.daugia.core;

public enum KetQuaPhien {
    PENDING_APPROVAL("Chờ duyệt"),
    APPROVED("Đã duyệt"),
    CANCELLED("Đã huỷ");

    private final String value;

    KetQuaPhien(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
