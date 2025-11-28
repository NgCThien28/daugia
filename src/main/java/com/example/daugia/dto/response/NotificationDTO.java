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
    private String tieude;
    private String noidung;
    private Timestamp thoigian;
    private String trangthai;
    private UserShortDTO taiKhoanQuanTri;
    private UserShortDTO taiKhoanKhachHang;
    private Long tongthongbaochuaxem;
    public NotificationDTO(String matb, UserShortDTO taiKhoanQuanTri, UserShortDTO taiKhoanKhachHang, String noidung, Timestamp thoigian) {
        this.matb = matb;
        this.taiKhoanQuanTri = taiKhoanQuanTri;
        this.taiKhoanKhachHang = taiKhoanKhachHang;
        this.noidung = noidung;
        this.thoigian = thoigian;
    }

    public NotificationDTO(String matb, String tieude, String noidung, Timestamp thoigian, String trangthai, Long tongthongbaochuaxem) {
        this.matb = matb;
        this.tieude = tieude;
        this.noidung = noidung;
        this.thoigian = thoigian;
        this.trangthai = trangthai;
        this.tongthongbaochuaxem = tongthongbaochuaxem;
    }

    public NotificationDTO(String matb, String tieude, String noidung, Timestamp thoigian, String trangthai) {
        this.matb = matb;
        this.tieude = tieude;
        this.noidung = noidung;
        this.thoigian = thoigian;
        this.trangthai = trangthai;
    }

    public NotificationDTO() {
    }

}
