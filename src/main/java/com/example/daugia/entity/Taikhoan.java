package com.example.daugia.entity;

import com.example.daugia.core.enums.TrangThaiTaiKhoan;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.util.List;

@Entity
public class Taikhoan {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
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
    @JsonIgnore
    private List<Phieuthanhtoantiencoc> phieuThanhToanTienCoc;

    @OneToMany(mappedBy = "taiKhoan", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Phieuthanhtoan> phieuThanhToan;

    private String ho;
    private String tenlot;
    private String ten;
    private String email;
    private String diachi;
    private String diachigiaohang;
    private String sdt;
    @JsonIgnore
    private String matkhau;
    private TrangThaiTaiKhoan trangthaidangnhap;

    public String getMatk() {
        return matk;
    }

    public void setMatk(String matk) {
        this.matk = matk;
    }

    public List<Thongbao> getThongBao() {
        return thongBao;
    }

    public void setThongBao(List<Thongbao> thongBao) {
        this.thongBao = thongBao;
    }

    public List<Phiendaugia> getPhienDauGia() {
        return phienDauGia;
    }

    public void setPhienDauGia(List<Phiendaugia> phienDauGia) {
        this.phienDauGia = phienDauGia;
    }

    public List<Phientragia> getPhienTraGia() {
        return phienTraGia;
    }

    public void setPhienTraGia(List<Phientragia> phienTraGia) {
        this.phienTraGia = phienTraGia;
    }

    public List<Sanpham> getSanPham() {
        return sanPham;
    }

    public void setSanPham(List<Sanpham> sanPham) {
        this.sanPham = sanPham;
    }

    public List<Phieuthanhtoantiencoc> getPhieuThanhToanTienCoc() {
        return phieuThanhToanTienCoc;
    }

    public void setPhieuThanhToanTienCoc(List<Phieuthanhtoantiencoc> phieuThanhToanTienCoc) {
        this.phieuThanhToanTienCoc = phieuThanhToanTienCoc;
    }

    public List<Phieuthanhtoan> getPhieuThanhToan() {
        return phieuThanhToan;
    }

    public void setPhieuThanhToan(List<Phieuthanhtoan> phieuThanhToan) {
        this.phieuThanhToan = phieuThanhToan;
    }

    public String getHo() {
        return ho;
    }

    public void setHo(String ho) {
        this.ho = ho;
    }

    public String getTenlot() {
        return tenlot;
    }

    public void setTenlot(String tenlot) {
        this.tenlot = tenlot;
    }

    public String getTen() {
        return ten;
    }

    public void setTen(String ten) {
        this.ten = ten;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDiachi() {
        return diachi;
    }

    public void setDiachi(String diachi) {
        this.diachi = diachi;
    }

    public String getDiachigiaohang() {
        return diachigiaohang;
    }

    public void setDiachigiaohang(String diachigiaohang) {
        this.diachigiaohang = diachigiaohang;
    }

    public String getSdt() {
        return sdt;
    }

    public void setSdt(String sdt) {
        this.sdt = sdt;
    }

    public String getMatkhau() {
        return matkhau;
    }

    public void setMatkhau(String matkhau) {
        this.matkhau = matkhau;
    }

    public TrangThaiTaiKhoan getTrangthaidangnhap() {
        return trangthaidangnhap;
    }

    public void setTrangthaidangnhap(TrangThaiTaiKhoan trangthaidangnhap) {
        this.trangthaidangnhap = trangthaidangnhap;
    }
}
