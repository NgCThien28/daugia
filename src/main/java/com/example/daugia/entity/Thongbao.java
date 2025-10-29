package com.example.daugia.entity;

import com.example.daugia.core.enums.TrangThaiThongBao;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import org.hibernate.annotations.GenericGenerator;

import java.sql.Timestamp;

@Entity
public class Thongbao {
    public static final String ID_PREFIX = "TB";
    @Id
    @GeneratedValue(generator = "prefix-id")
    @GenericGenerator(name = "prefix-id", strategy = "com.example.daugia.core.custom.PrefixIdGenerator")
    private String matb;

    @ManyToOne
    @JoinColumn(name = "makh", insertable = false, updatable = false)
    @JsonManagedReference
    private Taikhoan taiKhoan;

    @ManyToOne
    @JoinColumn(name = "maqtv", insertable = false, updatable = false)
    @JsonManagedReference
    private Taikhoanquantri taiKhoanQuanTri;

    private String noidung;
    private Timestamp thoigian;
    private TrangThaiThongBao trangthai;

    public String getMatb() {
        return matb;
    }

    public void setMatb(String matb) {
        this.matb = matb;
    }

    public Taikhoan getTaiKhoan() {
        return taiKhoan;
    }

    public void setTaiKhoan(Taikhoan taiKhoan) {
        this.taiKhoan = taiKhoan;
    }

    public Taikhoanquantri getTaiKhoanQuanTri() {
        return taiKhoanQuanTri;
    }

    public void setTaiKhoanQuanTri(Taikhoanquantri taiKhoanQuanTri) {
        this.taiKhoanQuanTri = taiKhoanQuanTri;
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

    public TrangThaiThongBao getTrangthai() {
        return trangthai;
    }

    public void setTrangthai(TrangThaiThongBao trangthai) {
        this.trangthai = trangthai;
    }
}
