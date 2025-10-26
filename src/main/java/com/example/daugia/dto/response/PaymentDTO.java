package com.example.daugia.dto.response;

import com.example.daugia.core.enums.TrangThaiPhieuThanhToan;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.sql.Timestamp;

@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class PaymentDTO {
    private String matt;
    private UserShortDTO taiKhoanKhachThanhToan;
    private AuctionDTO phienDauGia;
    private Timestamp thoigianthanhtoan;
    private TrangThaiPhieuThanhToan trangthai;

    public PaymentDTO(String matt, UserShortDTO taiKhoanKhachThanhToan, AuctionDTO phienDauGia, Timestamp thoigianthanhtoan, TrangThaiPhieuThanhToan trangthai) {
        this.matt = matt;
        this.taiKhoanKhachThanhToan = taiKhoanKhachThanhToan;
        this.phienDauGia = phienDauGia;
        this.thoigianthanhtoan = thoigianthanhtoan;
        this.trangthai = trangthai;
    }

    public PaymentDTO() {
    }

    public String getMatt() {
        return matt;
    }

    public void setMatt(String matt) {
        this.matt = matt;
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

    public TrangThaiPhieuThanhToan getTrangthai() {
        return trangthai;
    }

    public void setTrangthai(TrangThaiPhieuThanhToan trangthai) {
        this.trangthai = trangthai;
    }
}
