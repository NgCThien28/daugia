package com.example.daugia.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import java.sql.Timestamp;

@Entity
public class Thongbao {
    @Id
    private String matb;

    @ManyToOne
    @JoinColumn(name = "matk", insertable = false, updatable = false)
    private Taikhoan taiKhoan;

    @ManyToOne
    @JoinColumn(name = "matk", insertable = false, updatable = false)
    private Taikhoanquantri taiKhoanQuanTri;

    private String noidung;
    private Timestamp thoigian;
}
