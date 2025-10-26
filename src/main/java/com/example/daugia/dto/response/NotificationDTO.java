package com.example.daugia.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.sql.Timestamp;
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

    public String getMatb() {
        return matb;
    }

    public void setMatb(String matb) {
        this.matb = matb;
    }

    public UserShortDTO getTaiKhoanQuanTri() {
        return taiKhoanQuanTri;
    }

    public void setTaiKhoanQuanTri(UserShortDTO taiKhoanQuanTri) {
        this.taiKhoanQuanTri = taiKhoanQuanTri;
    }

    public UserShortDTO getTaiKhoanKhachHang() {
        return taiKhoanKhachHang;
    }

    public void setTaiKhoanKhachHang(UserShortDTO taiKhoanKhachHang) {
        this.taiKhoanKhachHang = taiKhoanKhachHang;
    }

    public String getNoidung() {
        return noidung;
    }

    public void setNoidung(String noidung) {
        this.noidung = noidung;
    }

    public Timestamp getThoigian() {
        return thoigian;
    }

    public void setThoigian(Timestamp thoigian) {
        this.thoigian = thoigian;
    }
}
