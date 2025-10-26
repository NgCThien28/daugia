package com.example.daugia.dto.request;

import com.example.daugia.core.enums.TrangThaiTaiKhoan;

public class TaikhoanCreationRequest {
    private String ho;
    private String tenlot;
    private String ten;
    private String email;
    private String diachi;
    private String diachigiaohang;
    private String sdt;
    private String matkhau;
    private TrangThaiTaiKhoan trangthaidangnhap;
    public String getHo() {
        return ho;
    }

    public void setHo(String ho) {
        this.ho = ho;
    }

    public String getTenlot() {
        return tenlot;
    }

    public void setTenlot(String tenlot) {
        this.tenlot = tenlot;
    }

    public String getTen() {
        return ten;
    }

    public void setTen(String ten) {
        this.ten = ten;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDiachi() {
        return diachi;
    }

    public void setDiachi(String diachi) {
        this.diachi = diachi;
    }

    public String getDiachigiaohang() {
        return diachigiaohang;
    }

    public void setDiachigiaohang(String diachigiaohang) {
        this.diachigiaohang = diachigiaohang;
    }

    public String getSdt() {
        return sdt;
    }

    public void setSdt(String sdt) {
        this.sdt = sdt;
    }

    public String getMatkhau() {
        return matkhau;
    }

    public void setMatkhau(String matkhau) {
        this.matkhau = matkhau;
    }

    public TrangThaiTaiKhoan getTrangthaidangnhap() {
        return trangthaidangnhap;
    }

    public void setTrangthaidangnhap(TrangThaiTaiKhoan trangthaidangnhap) {
        this.trangthaidangnhap = trangthaidangnhap;
    }
}
