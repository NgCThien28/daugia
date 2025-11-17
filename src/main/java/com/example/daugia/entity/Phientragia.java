package com.example.daugia.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Setter
@Getter
@Entity
public class Phientragia {
    public static final String ID_PREFIX = "TG";
    @Id
    @GeneratedValue(generator = "prefix-id")
    @GenericGenerator(name = "prefix-id", strategy = "com.example.daugia.core.custom.PrefixIdGenerator")
    private String maphientg;

    @ManyToOne
    @JoinColumn(name = "makh")
    @JsonBackReference
    private Taikhoan taiKhoan;

    @ManyToOne
    @JoinColumn(name = "maphiendg")
    @JsonBackReference
    private Phiendaugia phienDauGia;

    private BigDecimal sotien;
    private int solan;
    private Timestamp thoigian;
    private Timestamp thoigiancho;

}
