package com.example.daugia.entity;

import jakarta.persistence.*;

@Entity
public class Hinhanh {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String maanh;

    @ManyToOne
    @JoinColumn(name = "masp", insertable = false, updatable = false)
    private Sanpham sanPham;

    private String tenanh;
}
