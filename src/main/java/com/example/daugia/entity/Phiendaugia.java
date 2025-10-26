package com.example.daugia.entity;

import com.example.daugia.core.enums.KetQuaPhien;
import com.example.daugia.core.enums.TrangThaiPhienDauGia;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
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
    @JsonBackReference
    private Sanpham sanPham;

    @OneToOne(mappedBy = "phienDauGia")
    @JsonBackReference
    private Phieuthanhtoan phieuThanhToan;

    @ManyToOne
    @JoinColumn(name = "maqtv", insertable = false, updatable = false)
    @JsonBackReference
    private Taikhoanquantri taiKhoanQuanTri;

    @ManyToOne
    @JoinColumn(name = "makh", insertable = false, updatable = false)
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
    private double giakhoidiem;
    private double giatran;
    private double buocgia;
    private double giacaonhatdatduoc;
    private double tiencoc;
    private KetQuaPhien ketquaphien;
    private int slnguoithamgia;

    public String getMaphiendg() {
        return maphiendg;
    }

    public void setMaphiendg(String maphiendg) {
        this.maphiendg = maphiendg;
    }

    public Sanpham getSanPham() {
        return sanPham;
    }

    public void setSanPham(Sanpham sanPham) {
        this.sanPham = sanPham;
    }

    public Phieuthanhtoan getPhieuThanhToan() {
        return phieuThanhToan;
    }

    public void setPhieuThanhToan(Phieuthanhtoan phieuThanhToan) {
        this.phieuThanhToan = phieuThanhToan;
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

    public List<Phientragia> getPhienTraGia() {
        return phienTraGia;
    }

    public void setPhienTraGia(List<Phientragia> phienTraGia) {
        this.phienTraGia = phienTraGia;
    }

    public List<Phieuthanhtoantiencoc> getPhieuThanhToanTienCoc() {
        return phieuThanhToanTienCoc;
    }

    public void setPhieuThanhToanTienCoc(List<Phieuthanhtoantiencoc> phieuThanhToanTienCoc) {
        this.phieuThanhToanTienCoc = phieuThanhToanTienCoc;
    }

    public TrangThaiPhienDauGia getTrangthai() {
        return trangthai;
    }

    public void setTrangthai(TrangThaiPhienDauGia trangthai) {
        this.trangthai = trangthai;
    }

    public Timestamp getThoigianbd() {
        return thoigianbd;
    }

    public void setThoigianbd(Timestamp thoigianbd) {
        this.thoigianbd = thoigianbd;
    }

    public Timestamp getThoigiankt() {
        return thoigiankt;
    }

    public void setThoigiankt(Timestamp thoigiankt) {
        this.thoigiankt = thoigiankt;
    }

    public Timestamp getThoigianbddk() {
        return thoigianbddk;
    }

    public void setThoigianbddk(Timestamp thoigianbddk) {
        this.thoigianbddk = thoigianbddk;
    }

    public Timestamp getThoigianktdk() {
        return thoigianktdk;
    }

    public void setThoigianktdk(Timestamp thoigianktdk) {
        this.thoigianktdk = thoigianktdk;
    }

    public double getGiakhoidiem() {
        return giakhoidiem;
    }

    public void setGiakhoidiem(double giakhoidiem) {
        this.giakhoidiem = giakhoidiem;
    }

    public double getGiatran() {
        return giatran;
    }

    public void setGiatran(double giatran) {
        this.giatran = giatran;
    }

    public double getBuocgia() {
        return buocgia;
    }

    public void setBuocgia(double buocgia) {
        this.buocgia = buocgia;
    }

    public double getGiacaonhatdatduoc() {
        return giacaonhatdatduoc;
    }

    public void setGiacaonhatdatduoc(double giacaonhatdatduoc) {
        this.giacaonhatdatduoc = giacaonhatdatduoc;
    }

    public double getTiencoc() {
        return tiencoc;
    }

    public void setTiencoc(double tiencoc) {
        this.tiencoc = tiencoc;
    }

    public KetQuaPhien getKetquaphien() {
        return ketquaphien;
    }

    public void setKetquaphien(KetQuaPhien ketquaphien) {
        this.ketquaphien = ketquaphien;
    }

    public int getSlnguoithamgia() {
        return slnguoithamgia;
    }

    public void setSlnguoithamgia(int slnguoithamgia) {
        this.slnguoithamgia = slnguoithamgia;
    }
}
