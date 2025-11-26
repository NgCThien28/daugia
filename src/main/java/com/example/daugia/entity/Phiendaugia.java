package com.example.daugia.entity;

import com.example.daugia.core.enums.TrangThaiPhienDauGia;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;

@Setter
@Getter
@Entity
public class Phiendaugia {
    public static final String ID_PREFIX = "DG";
    @Id
    @GeneratedValue(generator = "prefix-id")
    @GenericGenerator(name = "prefix-id", strategy = "com.example.daugia.core.custom.PrefixIdGenerator")
    private String maphiendg;

    @OneToOne
    @JoinColumn(name = "masp")
    @JsonBackReference
    private Sanpham sanPham;

    @OneToOne(mappedBy = "phienDauGia")
    @JsonBackReference
    private Phieuthanhtoan phieuThanhToan;

    @ManyToOne
    @JoinColumn(name = "maqtv")
    @JsonBackReference
    private Taikhoanquantri taiKhoanQuanTri;

    @ManyToOne
    @JoinColumn(name = "makh")
    @JsonManagedReference
    private Taikhoan taiKhoan;

    @OneToMany(mappedBy = "phienDauGia", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Phientragia> phienTraGia;

    @OneToMany(mappedBy = "phienDauGia", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonBackReference
    private List<Phieuthanhtoantiencoc> phieuThanhToanTienCoc;

    private TrangThaiPhienDauGia trangthai;
    private Timestamp thoigianbd;
    private Timestamp thoigiankt;
    private Timestamp thoigianbddk;
    private Timestamp thoigianktdk;
    private BigDecimal giakhoidiem;
    private BigDecimal buocgia;
    private BigDecimal giacaonhatdatduoc;
    private BigDecimal tiencoc;
    private int slnguoithamgia;

}
