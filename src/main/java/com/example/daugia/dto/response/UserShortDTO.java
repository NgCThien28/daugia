package com.example.daugia.dto.response;

import com.example.daugia.core.enums.TrangThaiTaiKhoan;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class UserShortDTO {
    private String matk;
    private String ho;
    private String tenlot;
    private String ten;
    private String email;
    private String sdt;
    private String diachi;
    private TrangThaiTaiKhoan xacthuctaikhoan;
    private TrangThaiTaiKhoan trangthaidangnhap;

    public UserShortDTO(String matk, String ho, String tenlot, String ten, String email, String sdt) {
        this.matk = matk;
        this.ho = ho;
        this.tenlot = tenlot;
        this.ten = ten;
        this.email = email;
        this.sdt = sdt;
    }

    public UserShortDTO(String matk, String ho, String tenlot, String ten, TrangThaiTaiKhoan xacthuctaikhoan, TrangThaiTaiKhoan trangthaidangnhap) {
        this.matk = matk;
        this.ho = ho;
        this.tenlot = tenlot;
        this.ten = ten;
        this.xacthuctaikhoan = xacthuctaikhoan;
        this.trangthaidangnhap = trangthaidangnhap;
    }

    public UserShortDTO(String matk, String ho, String tenlot, String ten, String email, String sdt, String diachi) {
        this.matk = matk;
        this.ho = ho;
        this.tenlot = tenlot;
        this.ten = ten;
        this.email = email;
        this.sdt = sdt;
        this.diachi = diachi;
    }

    public UserShortDTO(String ho, String tenlot, String ten) {
        this.ho = ho;
        this.tenlot = tenlot;
        this.ten = ten;
    }

    public UserShortDTO(String matk, String ho, String tenlot, String ten) {
        this.matk = matk;
        this.ho = ho;
        this.tenlot = tenlot;
        this.ten = ten;
    }

    public UserShortDTO(String matk) {
        this.matk = matk;
    }

    public UserShortDTO(String matk, String email) {
        this.matk = matk;
        this.email = email;
    }

    public UserShortDTO() {
    }

}
