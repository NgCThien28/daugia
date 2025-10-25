package com.example.daugia.entity;

import jakarta.persistence.*;

import java.sql.Timestamp;

@Entity
public class Baocao {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String mabc;

    @ManyToOne
    @JoinColumn(name = "maqtv", insertable = false, updatable = false)
    private Taikhoanquantri taiKhoanQuanTri;

    private String noidung;
    private Timestamp thoigian;
}
