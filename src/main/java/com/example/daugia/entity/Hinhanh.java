package com.example.daugia.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

@Setter
@Getter
@Entity
public class Hinhanh {
    public static final String ID_PREFIX = "HA";
    @Id
    @GeneratedValue(generator = "prefix-id")
    @GenericGenerator(name = "prefix-id", strategy = "com.example.daugia.core.custom.PrefixIdGenerator")
    private String maanh;

    @ManyToOne
    @JoinColumn(name = "masp")
    @JsonBackReference
    private Sanpham sanPham;

    private String tenanh;

}
