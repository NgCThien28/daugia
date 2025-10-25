package com.example.daugia.entity;

import jakarta.persistence.*;

import java.sql.Time;
import java.sql.Timestamp;

@Entity
public class Phientragia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String maphientg;

    @ManyToOne
    @JoinColumn(name = "makh", insertable = false, updatable = false)
    private Taikhoan taiKhoan;

    @ManyToOne
    @JoinColumn(name = "maphiendg", insertable = false, updatable = false)
    private Phiendaugia phienDauGia;

    private double sotien;
    private int solan;
    private Timestamp thoigian;
    private Timestamp thoigiancho;
}
