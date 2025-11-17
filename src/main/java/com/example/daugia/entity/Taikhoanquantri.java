package com.example.daugia.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import java.util.List;

@Setter
@Getter
@Entity
public class Taikhoanquantri {
    public static final String ID_PREFIX = "QT";
    @Id
    @GeneratedValue(generator = "prefix-id")
    @GenericGenerator(name = "prefix-id", strategy = "com.example.daugia.core.custom.PrefixIdGenerator")
    private String matk;

    @OneToMany(mappedBy = "taiKhoanQuanTri", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonBackReference
    private List<Thongbao> thongBao;

    @OneToMany(mappedBy = "taiKhoanQuanTri", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonBackReference
    private List<Baocao> baoCao;

    @OneToMany(mappedBy = "taiKhoanQuanTri", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonBackReference
    private List<Sanpham> sanPham;

    @OneToMany(mappedBy = "taiKhoanQuanTri", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonBackReference
    private List<Phiendaugia> phienDauGia;

    private String ho;
    private String tenlot;
    private String ten;
    private String email;
    private String sdt;
    @JsonIgnore
    private String matkhau;

}
