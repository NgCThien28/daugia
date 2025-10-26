package com.example.daugia.dto.response;

import com.example.daugia.core.enums.TrangThaiPhieuThanhToan;
import com.example.daugia.core.enums.TrangThaiPhieuThanhToanTienCoc;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.sql.Timestamp;

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

    public String getMatc() {
        return matc;
    }

    public void setMatc(String matc) {
        this.matc = matc;
    }

    public UserShortDTO getTaiKhoanKhachThanhToan() {
        return taiKhoanKhachThanhToan;
    }

    public void setTaiKhoanKhachThanhToan(UserShortDTO taiKhoanKhachThanhToan) {
        this.taiKhoanKhachThanhToan = taiKhoanKhachThanhToan;
    }

    public AuctionDTO getPhienDauGia() {
        return phienDauGia;
    }

    public void setPhienDauGia(AuctionDTO phienDauGia) {
        this.phienDauGia = phienDauGia;
    }

    public Timestamp getThoigianthanhtoan() {
        return thoigianthanhtoan;
    }

    public void setThoigianthanhtoan(Timestamp thoigianthanhtoan) {
        this.thoigianthanhtoan = thoigianthanhtoan;
    }

    public TrangThaiPhieuThanhToanTienCoc getTrangthai() {
        return trangthai;
    }

    public void setTrangthai(TrangThaiPhieuThanhToanTienCoc trangthai) {
        this.trangthai = trangthai;
    }
}
