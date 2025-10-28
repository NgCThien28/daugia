package com.example.daugia.entity;

import com.example.daugia.core.enums.TrangThaiPhieuThanhToan;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import org.hibernate.annotations.GenericGenerator;

import java.sql.Timestamp;

@Entity
public class Phieuthanhtoan {
    public static final String ID_PREFIX = "TT";
    @Id
    @GeneratedValue(generator = "prefix-id")
    @GenericGenerator(name = "prefix-id", strategy = "com.example.daugia.core.custom.PrefixIdGenerator")
    private String matt;

    @OneToOne
    @JoinColumn(name = "maphiendg", nullable = false, unique = true)
    @JsonManagedReference
    private Phiendaugia phienDauGia;

    @ManyToOne
    @JoinColumn(name = "makh", insertable = false, updatable = false)
    @JsonManagedReference
    private Taikhoan taiKhoan;

    private Timestamp thoigianthanhtoan;
    private TrangThaiPhieuThanhToan trangthai;

    public String getMatt() {
        return matt;
    }

    public void setMatt(String matt) {
        this.matt = matt;
    }

    public Phiendaugia getPhienDauGia() {
        return phienDauGia;
    }

    public void setPhienDauGia(Phiendaugia phienDauGia) {
        this.phienDauGia = phienDauGia;
    }

    public Taikhoan getTaiKhoan() {
        return taiKhoan;
    }

    public void setTaiKhoan(Taikhoan taiKhoan) {
        this.taiKhoan = taiKhoan;
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
