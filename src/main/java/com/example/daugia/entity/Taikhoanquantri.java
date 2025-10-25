package com.example.daugia.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;

import java.util.List;

@Entity
public class Taikhoanquantri {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
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

    public List<Baocao> getBaoCao() {
        return baoCao;
    }

    public void setBaoCao(List<Baocao> baoCao) {
        this.baoCao = baoCao;
    }

    public List<Sanpham> getSanPham() {
        return sanPham;
    }

    public void setSanPham(List<Sanpham> sanPham) {
        this.sanPham = sanPham;
    }

    public List<Phiendaugia> getPhienDauGia() {
        return phienDauGia;
    }

    public void setPhienDauGia(List<Phiendaugia> phienDauGia) {
        this.phienDauGia = phienDauGia;
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
}
