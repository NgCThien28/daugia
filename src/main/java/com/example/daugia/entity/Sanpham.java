package com.example.daugia.entity;

import com.example.daugia.core.TrangThaiSanPham;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;

import java.util.List;
@Entity
public class Sanpham {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String masp;

    @ManyToOne
    @JoinColumn(name = "madm", insertable = false, updatable = false)
    private Danhmuc danhMuc;

    @ManyToOne
    @JoinColumn(name = "maqtv", insertable = false, updatable = false)
    private Taikhoanquantri taiKhoanQuanTri;

    @ManyToOne
    @JoinColumn(name = "makh", insertable = false, updatable = false)
    private Taikhoan taiKhoan;

    @OneToMany(mappedBy = "sanPham", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<Hinhanh> hinhAnh;

    @OneToOne(mappedBy = "sanPham")
    private Phiendaugia phienDauGia;

    private String tinhtrangsp;
    private String tensp;
    private TrangThaiSanPham trangthai;
}
