package com.example.daugia.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import java.sql.Timestamp;

@Setter
@Getter
@Entity
public class Baocao {
    public static final String ID_PREFIX = "BC";
    @Id
    @GeneratedValue(generator = "prefix-id")
    @GenericGenerator(name = "prefix-id", strategy = "com.example.daugia.core.custom.PrefixIdGenerator")
    private String mabc;

    @ManyToOne
    @JoinColumn(name = "maqtv")
    @JsonManagedReference
    private Taikhoanquantri taiKhoanQuanTri;

    private String noidung;
    private Timestamp thoigian;

}
