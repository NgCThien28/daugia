package com.example.daugia.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;
import java.sql.Timestamp;

@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class BiddingDTO {
    private String maphientg;
    private UserShortDTO taiKhoanNguoiRaGia;
    private AuctionDTO phienDauGia;
    private BigDecimal sotien;
    private int solan;
    private Timestamp thoigian;
    private Timestamp thoigiancho;

    public BiddingDTO(String maphientg, UserShortDTO taiKhoanNguoiRaGia,AuctionDTO phienDauGia, BigDecimal sotien, int solan, Timestamp thoigian, Timestamp thoigiancho) {
        this.maphientg = maphientg;
        this.taiKhoanNguoiRaGia = taiKhoanNguoiRaGia;
        this.phienDauGia = phienDauGia;
        this.sotien = sotien;
        this.solan = solan;
        this.thoigian = thoigian;
        this.thoigiancho = thoigiancho;
    }

    public BiddingDTO() {
    }

    public String getMaphientg() {
        return maphientg;
    }

    public void setMaphientg(String maphientg) {
        this.maphientg = maphientg;
    }

    public UserShortDTO getTaiKhoanNguoiRaGia() {
        return taiKhoanNguoiRaGia;
    }

    public void setTaiKhoanNguoiRaGia(UserShortDTO taiKhoanNguoiRaGia) {
        this.taiKhoanNguoiRaGia = taiKhoanNguoiRaGia;
    }

    public AuctionDTO getPhienDauGia() {
        return phienDauGia;
    }

    public void setPhienDauGia(AuctionDTO phienDauGia) {
        this.phienDauGia = phienDauGia;
    }

    public BigDecimal getSotien() {
        return sotien;
    }

    public void setSotien(BigDecimal sotien) {
        this.sotien = sotien;
    }

    public int getSolan() {
        return solan;
    }

    public void setSolan(int solan) {
        this.solan = solan;
    }

    public Timestamp getThoigian() {
        return thoigian;
    }

    public void setThoigian(Timestamp thoigian) {
        this.thoigian = thoigian;
    }

    public Timestamp getThoigiancho() {
        return thoigiancho;
    }

    public void setThoigiancho(Timestamp thoigiancho) {
        this.thoigiancho = thoigiancho;
    }
}
