package com.example.daugia.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import org.hibernate.annotations.GenericGenerator;

@Entity
public class Hinhanh {
    public static final String ID_PREFIX = "HA";
    @Id
    @GeneratedValue(generator = "prefix-id")
    @GenericGenerator(name = "prefix-id", strategy = "com.example.daugia.core.custom.PrefixIdGenerator")
    private String maanh;

    @ManyToOne
    @JoinColumn(name = "masp")
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
