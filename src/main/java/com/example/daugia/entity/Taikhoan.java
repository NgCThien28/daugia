package com.example.daugia.entity;

import com.example.daugia.core.enums.TrangThaiTaiKhoan;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import java.sql.Timestamp;
import java.util.List;

@Setter
@Getter
@Entity
public class Taikhoan {
    public static final String ID_PREFIX = "KH";
    @Id
    @GeneratedValue(generator = "prefix-id")
    @GenericGenerator(name = "prefix-id", strategy = "com.example.daugia.core.custom.PrefixIdGenerator")
    private String matk;

    @OneToMany(mappedBy = "taiKhoan", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Thongbao> thongBao;

    @OneToMany(mappedBy = "taiKhoan", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonBackReference
    private List<Phiendaugia> phienDauGia;

    @OneToMany(mappedBy = "taiKhoan", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Phientragia> phienTraGia;

    @OneToMany(mappedBy = "taiKhoan", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonBackReference
    private List<Sanpham> sanPham;

    @OneToMany(mappedBy = "taiKhoan", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonBackReference
    private List<Phieuthanhtoantiencoc> phieuThanhToanTienCoc;

    @OneToMany(mappedBy = "taiKhoan", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonBackReference
    private List<Phieuthanhtoan> phieuThanhToan;

    @ManyToOne
    @JoinColumn(name = "matp")
    @JsonManagedReference
    private Thanhpho thanhPho;

    private String ho;
    private String tenlot;
    private String ten;
    private String email;
    private String diachi;
    private String diachigiaohang;
    private String sdt;
    @JsonIgnore
    private String matkhau;
    private TrangThaiTaiKhoan xacthuctaikhoan;
    @JsonIgnore
    private String tokenxacthuc;
    @JsonIgnore
    private Timestamp tokenhethan;
    private TrangThaiTaiKhoan trangthaidangnhap;

}
