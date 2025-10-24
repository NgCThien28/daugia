package com.example.daugia.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;

import java.util.List;

@Entity
public class Taikhoanquantri {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String matk;

    @OneToMany(mappedBy = "taiKhoanQuanTri", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<Thongbao> thongBao;

    @OneToMany(mappedBy = "taiKhoanQuanTri", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<Baocao> baoCao;

    @OneToMany(mappedBy = "taiKhoanQuanTri", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<Sanpham> sanPham;

    private String ho;
    private String tenlot;
    private String ten;
    private String email;
    private String sdt;
    private String matkhau;
}
