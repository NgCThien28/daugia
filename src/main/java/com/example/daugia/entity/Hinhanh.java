package com.example.daugia.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

@Entity
public class Hinhanh {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String maanh;

    @ManyToOne
    @JoinColumn(name = "masp", insertable = false, updatable = false)
    @JsonBackReference
    private Sanpham sanPham;

    private String tenanh;

    public String getMaanh() {
        return maanh;
    }

    public void setMaanh(String maanh) {
        this.maanh = maanh;
    }

    public Sanpham getSanPham() {
        return sanPham;
    }

    public void setSanPham(Sanpham sanPham) {
        this.sanPham = sanPham;
    }

    public String getTenanh() {
        return tenanh;
    }

    public void setTenanh(String tenanh) {
        this.tenanh = tenanh;
    }
}
