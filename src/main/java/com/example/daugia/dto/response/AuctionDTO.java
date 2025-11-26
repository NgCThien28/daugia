package com.example.daugia.dto.response;

import com.example.daugia.core.enums.TrangThaiPhienDauGia;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Setter
@Getter
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class AuctionDTO {
    private String maphiendg;
    private UserShortDTO taiKhoanNguoiBan;
    private UserShortDTO taiKhoanQuanTri;
    private ProductDTO sanPham;
    private TrangThaiPhienDauGia trangthai;
    private Timestamp thoigianbd;
    private Timestamp thoigiankt;
    private Timestamp thoigianbddk;
    private Timestamp thoigianktdk;
    private BigDecimal giakhoidiem;
    private BigDecimal buocgia;
    private BigDecimal giacaonhatdatduoc;
    private BigDecimal tiencoc;
    private int slnguoithamgia;

    public AuctionDTO(String maphiendg, UserShortDTO taiKhoanNguoiBan, UserShortDTO taiKhoanQuanTri, TrangThaiPhienDauGia trangthai, Timestamp thoigianbd, Timestamp thoigiankt, Timestamp thoigianbddk, Timestamp thoigianktdk, BigDecimal giakhoidiem, BigDecimal buocgia, BigDecimal giacaonhatdatduoc, BigDecimal tiencoc, int slnguoithamgia) {
        this.maphiendg = maphiendg;
        this.taiKhoanNguoiBan = taiKhoanNguoiBan;
        this.taiKhoanQuanTri = taiKhoanQuanTri;
        this.trangthai = trangthai;
        this.thoigianbd = thoigianbd;
        this.thoigiankt = thoigiankt;
        this.thoigianbddk = thoigianbddk;
        this.thoigianktdk = thoigianktdk;
        this.giakhoidiem = giakhoidiem;
        this.buocgia = buocgia;
        this.giacaonhatdatduoc = giacaonhatdatduoc;
        this.tiencoc = tiencoc;
        this.slnguoithamgia = slnguoithamgia;
    }

    public AuctionDTO(String maphiendg, UserShortDTO taiKhoanNguoiBan,ProductDTO sanPham, TrangThaiPhienDauGia trangthai, Timestamp thoigianbd, Timestamp thoigiankt, Timestamp thoigianbddk, Timestamp thoigianktdk, BigDecimal giakhoidiem, BigDecimal buocgia, BigDecimal tiencoc) {
        this.maphiendg = maphiendg;
        this.taiKhoanNguoiBan = taiKhoanNguoiBan;
        this.sanPham = sanPham;
        this.trangthai = trangthai;
        this.thoigianbd = thoigianbd;
        this.thoigiankt = thoigiankt;
        this.thoigianbddk = thoigianbddk;
        this.thoigianktdk = thoigianktdk;
        this.giakhoidiem = giakhoidiem;
        this.buocgia = buocgia;
        this.tiencoc = tiencoc;
    }

    public AuctionDTO(String maphiendg) {
        this.maphiendg = maphiendg;
    }

    public AuctionDTO(String maphiendg, BigDecimal giacaonhatdatduoc) {
        this.maphiendg = maphiendg;
        this.giacaonhatdatduoc = giacaonhatdatduoc;
    }



    public AuctionDTO(BigDecimal tiencoc, String maphiendg) {
        this.tiencoc = tiencoc;
        this.maphiendg = maphiendg;
    }

    public AuctionDTO(String maphiendg, UserShortDTO taiKhoanNguoiBan, ProductDTO sanPham, TrangThaiPhienDauGia trangthai, Timestamp thoigianbd, Timestamp thoigiankt, Timestamp thoigianbddk, Timestamp thoigianktdk, BigDecimal giakhoidiem, BigDecimal buocgia, BigDecimal giacaonhatdatduoc, BigDecimal tiencoc, int slnguoithamgia) {
        this.maphiendg = maphiendg;
        this.taiKhoanNguoiBan = taiKhoanNguoiBan;
        this.sanPham = sanPham;
        this.trangthai = trangthai;
        this.thoigianbd = thoigianbd;
        this.thoigiankt = thoigiankt;
        this.thoigianbddk = thoigianbddk;
        this.thoigianktdk = thoigianktdk;
        this.giakhoidiem = giakhoidiem;
        this.buocgia = buocgia;
        this.giacaonhatdatduoc = giacaonhatdatduoc;
        this.tiencoc = tiencoc;
        this.slnguoithamgia = slnguoithamgia;
    }

    public AuctionDTO() {
    }
}
