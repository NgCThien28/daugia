package com.example.daugia.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;
@Setter
@Getter
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class NotificationDTO {
    private String matb;
    private UserShortDTO taiKhoanQuanTri;
    private UserShortDTO taiKhoanKhachHang;
    private String noidung;
    private Timestamp thoigian;

    public NotificationDTO(String matb, UserShortDTO taiKhoanQuanTri, UserShortDTO taiKhoanKhachHang, String noidung, Timestamp thoigian) {
        this.matb = matb;
        this.taiKhoanQuanTri = taiKhoanQuanTri;
        this.taiKhoanKhachHang = taiKhoanKhachHang;
        this.noidung = noidung;
        this.thoigian = thoigian;
    }

    public NotificationDTO() {
    }

}
