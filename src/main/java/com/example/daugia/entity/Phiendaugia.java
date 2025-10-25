package com.example.daugia.entity;

import com.example.daugia.core.KetQuaPhien;
import com.example.daugia.core.TrangThaiPhienDauGia;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;

import java.sql.Timestamp;
import java.util.List;

@Entity
public class Phiendaugia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String maphiendg;

    @OneToOne
    @JoinColumn(name = "masp", nullable = false, unique = true)
    private Sanpham sanPham;

    @OneToOne(mappedBy = "phienDauGia")
    private Phieuthanhtoan phieuThanhToan;

    @ManyToOne
    @JoinColumn(name = "maqtv", insertable = false, updatable = false)
    private Taikhoanquantri taiKhoanQuanTri;

    @ManyToOne
    @JoinColumn(name = "makh", insertable = false, updatable = false)
    private Taikhoan taiKhoan;

    @OneToMany(mappedBy = "phienDauGia", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<Phientragia> phienTraGia;

    @OneToMany(mappedBy = "phienDauGia", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<Phieuthanhtoantiencoc> phieuThanhToanTienCoc;

    private TrangThaiPhienDauGia trangthai;
    private Timestamp thoigianbd;
    private Timestamp thoigiankt;
    private Timestamp thoigianbddk;
    private Timestamp thoigianktdk;
    private double giakhoidiem;
    private double giatran;
    private double buocgia;
    private double giacaonhatdatduoc;
    private double tiencoc;
    private KetQuaPhien ketquaphien;
    private int soluongnguoithamgia;
}
