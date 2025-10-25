package com.example.daugia.entity;

import com.example.daugia.core.TrangThaiPhieuThanhToan;
import jakarta.persistence.*;

import java.sql.Timestamp;

@Entity
public class Phieuthanhtoan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String matt;

    @OneToOne
    @JoinColumn(name = "maphiendg", nullable = false, unique = true)
    private Phiendaugia phienDauGia;

    @ManyToOne
    @JoinColumn(name = "makh", insertable = false, updatable = false)
    private Taikhoan taiKhoan;

    private Timestamp thoigianthanhtoan;
    private TrangThaiPhieuThanhToan trangthai;
}
