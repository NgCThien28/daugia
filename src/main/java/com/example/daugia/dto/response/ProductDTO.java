package com.example.daugia.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Setter
@Getter
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class ProductDTO {
    private String masp;
    private String madm;
    private UserShortDTO taiKhoanNguoiBan;
    private List<ImageDTO> hinhAnh;
    private CityDTO thanhpho;
    private String tinhtrangsp;
    private String tensp;
    private String trangthai;
    private BigDecimal giamongdoi;
    private BigDecimal hoahong;

    public ProductDTO(String masp, UserShortDTO taiKhoan,CityDTO thanhpho, List<ImageDTO> hinhAnh, String tinhtrangsp, String tensp, String trangthai, BigDecimal giamongdoi, BigDecimal hoahong) {
        this.masp = masp;
        this.taiKhoanNguoiBan = taiKhoan;
        this.thanhpho = thanhpho;
        this.hinhAnh = hinhAnh;
        this.tinhtrangsp = tinhtrangsp;
        this.tensp = tensp;
        this.trangthai = trangthai;
        this.giamongdoi = giamongdoi;
        this.hoahong = hoahong;
    }

    public ProductDTO(String tensp, String masp,String madm,CityDTO thanhpho, List<ImageDTO> hinhAnh,String tinhtrangsp) {
        this.tensp = tensp;
        this.masp = masp;
        this.madm = madm;
        this.thanhpho = thanhpho;
        this.hinhAnh = hinhAnh;
        this.tinhtrangsp = tinhtrangsp;
    }

    public ProductDTO(String masp, String tensp) {
        this.masp = masp;
        this.tensp = tensp;
    }



    public ProductDTO(String tensp) {
        this.tensp = tensp;
    }

}
