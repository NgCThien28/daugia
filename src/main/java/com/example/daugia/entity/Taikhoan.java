package com.example.daugia.entity;

import com.example.daugia.core.TrangThaiTaiKhoan;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;

import java.util.List;

@Entity
public class Taikhoan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String matk;

    @OneToMany(mappedBy = "taiKhoan", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<Thongbao> thongBao;

    @OneToMany(mappedBy = "taiKhoan", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<Phiendaugia> phienDauGia;

    @OneToMany(mappedBy = "taiKhoan", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<Phientragia> phienTraGia;

    @OneToMany(mappedBy = "taiKhoan", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<Sanpham> sanPham;

    @OneToMany(mappedBy = "taiKhoan", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<Phieuthanhtoantiencoc> phieuThanhToanTienCoc;

    @OneToMany(mappedBy = "taiKhoan", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<Phieuthanhtoan> phieuThanhToan;

    private String ho;
    private String tenlot;
    private String ten;
    private String email;
    private String diachi;
    private String diachigiaohang;
    private String sdt;
    private String matkhau;
    private TrangThaiTaiKhoan trangthai;
}
