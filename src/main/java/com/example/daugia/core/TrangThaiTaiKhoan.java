package com.example.daugia.core;

public enum TrangThaiTaiKhoan {
    ONLINE("Đang hoạt động"),
    OFFLINE("Ngoại tuyến");

    private final String value;

    TrangThaiTaiKhoan(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
