package com.example.daugia.entity;

import com.example.daugia.core.enums.TrangThaiPhieuThanhToanTienCoc;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import java.sql.Timestamp;

@Setter
@Getter
@Entity
public class Phieuthanhtoantiencoc {
    public static final String ID_PREFIX = "TC";
    @Id
    @GeneratedValue(generator = "prefix-id")
    @GenericGenerator(name = "prefix-id", strategy = "com.example.daugia.core.custom.PrefixIdGenerator")
    private String matc;

    @ManyToOne
    @JoinColumn(name = "maphiendg")
    @JsonManagedReference
    private Phiendaugia phienDauGia;

    @ManyToOne
    @JoinColumn(name = "makh")
    @JsonManagedReference
    private Taikhoan taiKhoan;

    private Timestamp thoigianthanhtoan;
    private TrangThaiPhieuThanhToanTienCoc trangthai;
    private String vnptransactionno;
    private String bankcode;
    private String raw;

}
