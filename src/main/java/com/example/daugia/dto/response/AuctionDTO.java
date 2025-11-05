package com.example.daugia.dto.response;

import com.example.daugia.core.enums.KetQuaPhien;
import com.example.daugia.core.enums.TrangThaiPhienDauGia;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.sql.Timestamp;

@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class AuctionDTO {
    private String maphiendg;
    private UserShortDTO taiKhoanNguoiBan;
    private UserShortDTO taiKhoanQuanTri;
    private ProductDTO sanPham;
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

    public AuctionDTO(String maphiendg, UserShortDTO taiKhoanNguoiBan, UserShortDTO taiKhoanQuanTri, TrangThaiPhienDauGia trangthai, Timestamp thoigianbd, Timestamp thoigiankt, Timestamp thoigianbddk, Timestamp thoigianktdk, double giakhoidiem, double giatran, double buocgia, double giacaonhatdatduoc, double tiencoc, KetQuaPhien ketquaphien, int slnguoithamgia) {
        this.maphiendg = maphiendg;
        this.taiKhoanNguoiBan = taiKhoanNguoiBan;
        this.taiKhoanQuanTri = taiKhoanQuanTri;
        this.trangthai = trangthai;
        this.thoigianbd = thoigianbd;
        this.thoigiankt = thoigiankt;
        this.thoigianbddk = thoigianbddk;
        this.thoigianktdk = thoigianktdk;
        this.giakhoidiem = giakhoidiem;
        this.giatran = giatran;
        this.buocgia = buocgia;
        this.giacaonhatdatduoc = giacaonhatdatduoc;
        this.tiencoc = tiencoc;
        this.ketquaphien = ketquaphien;
        this.slnguoithamgia = slnguoithamgia;
    }

    public AuctionDTO(String maphiendg, UserShortDTO taiKhoanNguoiBan,ProductDTO sanPham, TrangThaiPhienDauGia trangthai, Timestamp thoigianbd, Timestamp thoigiankt, Timestamp thoigianbddk, Timestamp thoigianktdk, double giakhoidiem, double giatran, double buocgia, double tiencoc) {
        this.maphiendg = maphiendg;
        this.taiKhoanNguoiBan = taiKhoanNguoiBan;
        this.sanPham = sanPham;
        this.trangthai = trangthai;
        this.thoigianbd = thoigianbd;
        this.thoigiankt = thoigiankt;
        this.thoigianbddk = thoigianbddk;
        this.thoigianktdk = thoigianktdk;
        this.giakhoidiem = giakhoidiem;
        this.giatran = giatran;
        this.buocgia = buocgia;
        this.tiencoc = tiencoc;
    }

    public AuctionDTO(String maphiendg) {
        this.maphiendg = maphiendg;
    }

    public AuctionDTO(String maphiendg, double giacaonhatdatduoc) {
        this.maphiendg = maphiendg;
        this.giacaonhatdatduoc = giacaonhatdatduoc;
    }

    public AuctionDTO() {
    }

    public String getMaphiendg() {
        return maphiendg;
    }

    public void setMaphiendg(String maphiendg) {
        this.maphiendg = maphiendg;
    }

    public UserShortDTO getTaiKhoanNguoiBan() {
        return taiKhoanNguoiBan;
    }

    public void setTaiKhoanNguoiBan(UserShortDTO taiKhoanNguoiBan) {
        this.taiKhoanNguoiBan = taiKhoanNguoiBan;
    }

    public UserShortDTO getTaiKhoanQuanTri() {
        return taiKhoanQuanTri;
    }

    public void setTaiKhoanQuanTri(UserShortDTO taiKhoanQuanTri) {
        this.taiKhoanQuanTri = taiKhoanQuanTri;
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

    public ProductDTO getSanPham() {
        return sanPham;
    }

    public void setSanPham(ProductDTO sanPham) {
        this.sanPham = sanPham;
    }
}
