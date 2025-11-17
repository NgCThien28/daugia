package com.example.daugia.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

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

    public ProductDTO(String masp, UserShortDTO taiKhoan,CityDTO thanhpho, List<ImageDTO> hinhAnh, String tinhtrangsp, String tensp, String trangthai) {
        this.masp = masp;
        this.taiKhoanNguoiBan = taiKhoan;
        this.thanhpho = thanhpho;
        this.hinhAnh = hinhAnh;
        this.tinhtrangsp = tinhtrangsp;
        this.tensp = tensp;
        this.trangthai = trangthai;
    }

    public ProductDTO(String tensp, String masp,String madm,CityDTO thanhpho, List<ImageDTO> hinhAnh) {
        this.tensp = tensp;
        this.masp = masp;
        this.madm = madm;
        this.thanhpho = thanhpho;
        this.hinhAnh = hinhAnh;
    }

    public ProductDTO(String masp, String tensp) {
        this.masp = masp;
        this.tensp = tensp;
    }

    public ProductDTO() {
    }

}
