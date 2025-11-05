package com.example.daugia.dto.request;

import com.example.daugia.core.enums.KetQuaPhien;
import com.example.daugia.core.enums.TrangThaiPhienDauGia;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.sql.Timestamp;

public class PhiendaugiaCreationRequest {
    private String masp;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Timestamp thoigianbd;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Timestamp thoigiankt;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Timestamp thoigianbddk;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Timestamp thoigianktdk;
    private double giakhoidiem;
    private double giatran;
    private double buocgia;
    private double tiencoc;
    private KetQuaPhien ketquaphien;

    public String getMasp() {
        return masp;
    }

    public void setMasp(String masp) {
        this.masp = masp;
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
}
