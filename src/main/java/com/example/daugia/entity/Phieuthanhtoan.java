package com.example.daugia.entity;

import com.example.daugia.core.enums.TrangThaiPhieuThanhToan;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Setter
@Getter
@Entity
public class Phieuthanhtoan {
    public static final String ID_PREFIX = "TT";
    @Id
    @GeneratedValue(generator = "prefix-id")
    @GenericGenerator(name = "prefix-id", strategy = "com.example.daugia.core.custom.PrefixIdGenerator")
    private String matt;

    @OneToOne
    @JoinColumn(name = "maphiendg", nullable = false, unique = true)
    @JsonManagedReference
    private Phiendaugia phienDauGia;

    @ManyToOne
    @JoinColumn(name = "makh", nullable = false, unique = true)
    @JsonManagedReference
    private Taikhoan taiKhoan;
    private BigDecimal sotien;
    private Timestamp thoigianthanhtoan;
    private TrangThaiPhieuThanhToan trangthai;
    private String vnptransactionno;
    private String bankcode;
    private String raw;

}
