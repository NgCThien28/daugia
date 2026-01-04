package com.example.daugia.core.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import org.apache.poi.ss.usermodel.IndexedColors;

@Getter
public enum TrangThaiPhieuThanhToan {
    UNPAID("Chưa thanh toán", IndexedColors.LIGHT_YELLOW),
    PAID("Đã thanh toán", IndexedColors.GREEN),
    CANCELLED("Bị hủy", IndexedColors.RED);

    private final String value;
    private final IndexedColors color;

    TrangThaiPhieuThanhToan(String value, IndexedColors color) {
        this.value = value;
        this.color = color;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
