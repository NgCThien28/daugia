package com.example.daugia.dto.response;

import com.example.daugia.core.enums.TrangThaiPhieuThanhToan;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Setter
@Getter
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class PaymentDTO {
    private String matt;
    private UserShortDTO taiKhoanKhachThanhToan;
    private AuctionDTO phienDauGia;
    private Timestamp thoigianthanhtoan;
    private TrangThaiPhieuThanhToan trangthai;
    private BigDecimal sotien;

    public PaymentDTO(String matt, UserShortDTO taiKhoanKhachThanhToan, AuctionDTO phienDauGia, Timestamp thoigianthanhtoan, TrangThaiPhieuThanhToan trangthai, BigDecimal sotien) {
        this.matt = matt;
        this.taiKhoanKhachThanhToan = taiKhoanKhachThanhToan;
        this.phienDauGia = phienDauGia;
        this.thoigianthanhtoan = thoigianthanhtoan;
        this.trangthai = trangthai;
        this.sotien = sotien;
    }

    public PaymentDTO() {
    }

}
