package com.example.daugia.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
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
    private BigDecimal giakhoidiem;
    private BigDecimal giatran;
    private BigDecimal buocgia;
    private BigDecimal tiencoc;

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

    public BigDecimal getGiakhoidiem() {
        return giakhoidiem;
    }

    public void setGiakhoidiem(BigDecimal giakhoidiem) {
        this.giakhoidiem = giakhoidiem;
    }

    public BigDecimal getGiatran() {
        return giatran;
    }

    public void setGiatran(BigDecimal giatran) {
        this.giatran = giatran;
    }

    public BigDecimal getBuocgia() {
        return buocgia;
    }

    public void setBuocgia(BigDecimal buocgia) {
        this.buocgia = buocgia;
    }

    public BigDecimal getTiencoc() {
        return tiencoc;
    }

    public void setTiencoc(BigDecimal tiencoc) {
        this.tiencoc = tiencoc;
    }

}
