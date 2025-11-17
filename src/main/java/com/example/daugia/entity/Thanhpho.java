package com.example.daugia.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import java.util.List;

@Setter
@Getter
@Entity
public class Thanhpho {
    public static final String ID_PREFIX = "TP";
    @Id
    @GeneratedValue(generator = "prefix-id")
    @GenericGenerator(name = "prefix-id", strategy = "com.example.daugia.core.custom.PrefixIdGenerator")
    private String matp;
    @OneToMany(mappedBy = "thanhPho", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonBackReference
    private List<Taikhoan> taikhoanList;
    @OneToMany(mappedBy = "thanhPho", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonBackReference
    private List<Sanpham> sanphamList;

    private String tentp;

}
