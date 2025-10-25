package com.example.daugia.entity;

import jakarta.persistence.*;

import java.sql.Timestamp;

@Entity
public class Thongbao {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String matb;

    @ManyToOne
    @JoinColumn(name = "matk", insertable = false, updatable = false)
    private Taikhoan taiKhoan;

    @ManyToOne
    @JoinColumn(name = "maqt", insertable = false, updatable = false)
    private Taikhoanquantri taiKhoanQuanTri;

    private String noidung;
    private Timestamp thoigian;
}
