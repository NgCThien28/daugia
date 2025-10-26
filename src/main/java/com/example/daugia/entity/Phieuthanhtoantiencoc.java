package com.example.daugia.entity;

import com.example.daugia.core.enums.TrangThaiPhieuThanhToanTienCoc;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;

import java.sql.Timestamp;

@Entity
public class Phieuthanhtoantiencoc {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String matc;


    @ManyToOne
    @JoinColumn(name = "maphiendg", insertable = false, updatable = false)
    @JsonManagedReference
    private Phiendaugia phienDauGia;

    @ManyToOne
    @JoinColumn(name = "makh", insertable = false, updatable = false)
    @JsonManagedReference
    private Taikhoan taiKhoan;

    private Timestamp thoigianthanhtoan;
    private TrangThaiPhieuThanhToanTienCoc trangthai;

    public String getMatc() {
        return matc;
    }

    public void setMatc(String matc) {
        this.matc = matc;
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

    public TrangThaiPhieuThanhToanTienCoc getTrangthai() {
        return trangthai;
    }

    public void setTrangthai(TrangThaiPhieuThanhToanTienCoc trangthai) {
        this.trangthai = trangthai;
    }
}
