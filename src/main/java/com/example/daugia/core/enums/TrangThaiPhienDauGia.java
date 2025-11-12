package com.example.daugia.core.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum TrangThaiPhienDauGia {
    PENDING_APPROVAL("Chờ duyệt"),
    APPROVED("Đã duyệt"),
    NOT_STARTED("Chưa bắt đầu"),
    IN_PROGRESS("Đang diễn ra"),
    WAITING_FOR_PAYMENT("Chờ thanh toán"),
    SUCCESS("Thành công"),
    FAILED("Thất bại"),
    CANCELLED("Đã huỷ");

    private final String value;

    TrangThaiPhienDauGia(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
