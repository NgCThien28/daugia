package com.example.daugia.entity;

import com.example.daugia.core.enums.TrangThaiSanPham;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
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
    @JsonBackReference
    private Danhmuc danhMuc;

    @ManyToOne
    @JoinColumn(name = "maqtv", insertable = false, updatable = false)
    @JsonManagedReference
    private Taikhoanquantri taiKhoanQuanTri;

    @ManyToOne
    @JoinColumn(name = "makh", insertable = false, updatable = false)
    @JsonManagedReference
    private Taikhoan taiKhoan;

    @OneToMany(mappedBy = "sanPham", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<Hinhanh> hinhAnh;

    @OneToOne(mappedBy = "sanPham")
    @JsonIgnore
    private Phiendaugia phienDauGia;

    private String tinhtrangsp;
    private String tensp;
    private TrangThaiSanPham trangthai;

    public String getMasp() {
        return masp;
    }

    public void setMasp(String masp) {
        this.masp = masp;
    }

    public Danhmuc getDanhMuc() {
        return danhMuc;
    }

    public void setDanhMuc(Danhmuc danhMuc) {
        this.danhMuc = danhMuc;
    }

    public Taikhoanquantri getTaiKhoanQuanTri() {
        return taiKhoanQuanTri;
    }

    public void setTaiKhoanQuanTri(Taikhoanquantri taiKhoanQuanTri) {
        this.taiKhoanQuanTri = taiKhoanQuanTri;
    }

    public Taikhoan getTaiKhoan() {
        return taiKhoan;
    }

    public void setTaiKhoan(Taikhoan taiKhoan) {
        this.taiKhoan = taiKhoan;
    }

    public List<Hinhanh> getHinhAnh() {
        return hinhAnh;
    }

    public void setHinhAnh(List<Hinhanh> hinhAnh) {
        this.hinhAnh = hinhAnh;
    }

    public Phiendaugia getPhienDauGia() {
        return phienDauGia;
    }

    public void setPhienDauGia(Phiendaugia phienDauGia) {
        this.phienDauGia = phienDauGia;
    }

    public String getTinhtrangsp() {
        return tinhtrangsp;
    }

    public void setTinhtrangsp(String tinhtrangsp) {
        this.tinhtrangsp = tinhtrangsp;
    }

    public String getTensp() {
        return tensp;
    }

    public void setTensp(String tensp) {
        this.tensp = tensp;
    }

    public TrangThaiSanPham getTrangthai() {
        return trangthai;
    }

    public void setTrangthai(TrangThaiSanPham trangthai) {
        this.trangthai = trangthai;
    }
}
