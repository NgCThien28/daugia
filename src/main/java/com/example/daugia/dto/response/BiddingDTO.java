package com.example.daugia.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Setter
@Getter
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

}
