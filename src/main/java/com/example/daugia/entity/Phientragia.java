package com.example.daugia.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import org.hibernate.annotations.GenericGenerator;

import java.sql.Timestamp;

@Entity
public class Phientragia {
    public static final String ID_PREFIX = "TG";
    @Id
    @GeneratedValue(generator = "prefix-id")
    @GenericGenerator(name = "prefix-id", strategy = "com.example.daugia.core.custom.PrefixIdGenerator")
    private String maphientg;

    @ManyToOne
    @JoinColumn(name = "makh")
    @JsonBackReference
    private Taikhoan taiKhoan;

    @ManyToOne
    @JoinColumn(name = "maphiendg")
    @JsonBackReference
    private Phiendaugia phienDauGia;

    private double sotien;
    private int solan;
    private Timestamp thoigian;
    private Timestamp thoigiancho;

    public String getMaphientg() {
        return maphientg;
    }

    public void setMaphientg(String maphientg) {
        this.maphientg = maphientg;
    }

    public Taikhoan getTaiKhoan() {
        return taiKhoan;
    }

    public void setTaiKhoan(Taikhoan taiKhoan) {
        this.taiKhoan = taiKhoan;
    }

    public Phiendaugia getPhienDauGia() {
        return phienDauGia;
    }

    public void setPhienDauGia(Phiendaugia phienDauGia) {
        this.phienDauGia = phienDauGia;
    }

    public double getSotien() {
        return sotien;
    }

    public void setSotien(double sotien) {
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
