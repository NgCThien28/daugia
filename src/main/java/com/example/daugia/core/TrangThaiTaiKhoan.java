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

    public static TrangThaiTaiKhoan fromString(String value) {
        for (TrangThaiTaiKhoan flightStatus : TrangThaiTaiKhoan.values()) {
            if (flightStatus.value.equalsIgnoreCase(value)) {
                return flightStatus;
            }
        }
        throw new IllegalArgumentException("Unknown role: " + value);
    }
}
