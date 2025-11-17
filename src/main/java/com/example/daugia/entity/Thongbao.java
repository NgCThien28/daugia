package com.example.daugia.entity;

import com.example.daugia.core.enums.TrangThaiThongBao;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import java.sql.Timestamp;

@Setter
@Getter
@Entity
public class Thongbao {
    public static final String ID_PREFIX = "TB";
    @Id
    @GeneratedValue(generator = "prefix-id")
    @GenericGenerator(name = "prefix-id", strategy = "com.example.daugia.core.custom.PrefixIdGenerator")
    private String matb;

    @ManyToOne
    @JoinColumn(name = "makh", insertable = false, updatable = false)
    @JsonManagedReference
    private Taikhoan taiKhoan;

    @ManyToOne
    @JoinColumn(name = "maqtv", insertable = false, updatable = false)
    @JsonManagedReference
    private Taikhoanquantri taiKhoanQuanTri;

    private String noidung;
    private Timestamp thoigian;
    private TrangThaiThongBao trangthai;

}
