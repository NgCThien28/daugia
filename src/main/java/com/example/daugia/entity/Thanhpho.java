package com.example.daugia.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import org.hibernate.annotations.GenericGenerator;

import java.util.List;

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

    public String getMatp() {
        return matp;
    }

    public void setMatp(String matp) {
        this.matp = matp;
    }

    public List<Taikhoan> getTaikhoanList() {
        return taikhoanList;
    }

    public void setTaikhoanList(List<Taikhoan> taikhoanList) {
        this.taikhoanList = taikhoanList;
    }

    public List<Sanpham> getSanphamList() {
        return sanphamList;
    }

    public void setSanphamList(List<Sanpham> sanphamList) {
        this.sanphamList = sanphamList;
    }

    public String getTentp() {
        return tentp;
    }

    public void setTentp(String tentp) {
        this.tentp = tentp;
    }
}
