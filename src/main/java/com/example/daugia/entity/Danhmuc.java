package com.example.daugia.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;

import java.util.List;

@Entity
public class Danhmuc {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String madm;

    @OneToMany(mappedBy = "danhMuc", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Sanpham> sanPham;

    private String tendm;

    public String getMadm() {
        return madm;
    }

    public void setMadm(String madm) {
        this.madm = madm;
    }

    public List<Sanpham> getSanPham() {
        return sanPham;
    }

    public void setSanPham(List<Sanpham> sanPham) {
        this.sanPham = sanPham;
    }

    public String getTendm() {
        return tendm;
    }

    public void setTendm(String tendm) {
        this.tendm = tendm;
    }
}
