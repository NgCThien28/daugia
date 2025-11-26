package com.example.daugia.entity;

import com.example.daugia.core.enums.TrangThaiSanPham;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import java.math.BigDecimal;
import java.util.List;
@Setter
@Getter
@Entity
public class Sanpham {
    public static final String ID_PREFIX = "SP";
    @Id
    @GeneratedValue(generator = "prefix-id")
    @GenericGenerator(name = "prefix-id", strategy = "com.example.daugia.core.custom.PrefixIdGenerator")
    private String masp;

    @ManyToOne
    @JoinColumn(name = "madm")
    @JsonManagedReference
    private Danhmuc danhMuc;

    @ManyToOne
    @JoinColumn(name = "maqtv", insertable = false, updatable = false)
    @JsonManagedReference
    private Taikhoanquantri taiKhoanQuanTri;

    @ManyToOne
    @JoinColumn(name = "makh")
    @JsonManagedReference
    private Taikhoan taiKhoan;

    @OneToMany(mappedBy = "sanPham", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<Hinhanh> hinhAnh;

    @OneToOne(mappedBy = "sanPham")
    @JsonIgnore
    private Phiendaugia phienDauGia;

    @ManyToOne
    @JoinColumn(name = "matp")
    @JsonManagedReference
    private Thanhpho thanhPho;
    private String tinhtrangsp;
    private String tensp;
    private BigDecimal giamongdoi;
    private BigDecimal hoahong;
    private TrangThaiSanPham trangthai;

}
