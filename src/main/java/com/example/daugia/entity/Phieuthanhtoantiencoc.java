package com.example.daugia.entity;

import com.example.daugia.core.TrangThaiPhieuThanhToanTienCoc;
import jakarta.persistence.*;

import java.sql.Timestamp;

@Entity
public class Phieuthanhtoantiencoc {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String matc;

    @ManyToOne
    @JoinColumn(name = "maphiendg", insertable = false, updatable = false)
    private Phiendaugia phienDauGia;

    @ManyToOne
    @JoinColumn(name = "makh", insertable = false, updatable = false)
    private Taikhoan taiKhoan;

    private Timestamp thoigianthanhtoan;
    private TrangThaiPhieuThanhToanTienCoc trangthai;
}
