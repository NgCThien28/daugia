package com.example.daugia.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;

import java.sql.Timestamp;

@Entity
public class Baocao {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String mabc;

    @ManyToOne
    @JoinColumn(name = "maqtv", insertable = false, updatable = false)
    @JsonManagedReference
    private Taikhoanquantri taiKhoanQuanTri;

    private String noidung;
    private Timestamp thoigian;

    public String getMabc() {
        return mabc;
    }

    public void setMabc(String mabc) {
        this.mabc = mabc;
    }

    public Taikhoanquantri getTaiKhoanQuanTri() {
        return taiKhoanQuanTri;
    }

    public void setTaiKhoanQuanTri(Taikhoanquantri taiKhoanQuanTri) {
        this.taiKhoanQuanTri = taiKhoanQuanTri;
    }

    public String getNoidung() {
        return noidung;
    }

    public void setNoidung(String noidung) {
        this.noidung = noidung;
    }

    public Timestamp getThoigian() {
        return thoigian;
    }

    public void setThoigian(Timestamp thoigian) {
        this.thoigian = thoigian;
    }
}
