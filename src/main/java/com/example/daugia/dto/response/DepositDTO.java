package com.example.daugia.dto.response;

import com.example.daugia.core.enums.TrangThaiPhieuThanhToanTienCoc;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Setter
@Getter
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class DepositDTO {
    private String matc;
    private UserShortDTO taiKhoanKhachThanhToan;
    private AuctionDTO phienDauGia;
    private Timestamp thoigianthanhtoan;
    private TrangThaiPhieuThanhToanTienCoc trangthai;

    public DepositDTO(String matc, UserShortDTO taiKhoanKhachThanhToan, AuctionDTO phienDauGia, Timestamp thoigianthanhtoan, TrangThaiPhieuThanhToanTienCoc trangthai) {
        this.matc = matc;
        this.taiKhoanKhachThanhToan = taiKhoanKhachThanhToan;
        this.phienDauGia = phienDauGia;
        this.thoigianthanhtoan = thoigianthanhtoan;
        this.trangthai = trangthai;
    }

    public DepositDTO() {
    }

}
