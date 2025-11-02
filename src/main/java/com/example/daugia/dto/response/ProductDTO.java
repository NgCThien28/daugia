package com.example.daugia.dto.response;

import com.example.daugia.entity.Thanhpho;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class ProductDTO {
    private String masp;
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

    public ProductDTO() {
    }

    public String getMasp() {
        return masp;
    }

    public void setMasp(String masp) {
        this.masp = masp;
    }

    public UserShortDTO getTaiKhoanNguoiBan() {
        return taiKhoanNguoiBan;
    }

    public void setTaiKhoanNguoiBan(UserShortDTO taiKhoanNguoiBan) {
        this.taiKhoanNguoiBan = taiKhoanNguoiBan;
    }

    public List<ImageDTO> getHinhAnh() {
        return hinhAnh;
    }

    public void setHinhAnh(List<ImageDTO> hinhAnh) {
        this.hinhAnh = hinhAnh;
    }

    public String getTinhtrangsp() {
        return tinhtrangsp;
    }

    public void setTinhtrangsp(String tinhtrangsp) {
        this.tinhtrangsp = tinhtrangsp;
    }

    public String getTensp() {
        return tensp;
    }

    public void setTensp(String tensp) {
        this.tensp = tensp;
    }

    public String getTrangthai() {
        return trangthai;
    }

    public void setTrangthai(String trangthai) {
        this.trangthai = trangthai;
    }

    public CityDTO getThanhpho() {
        return thanhpho;
    }

    public void setThanhpho(CityDTO thanhpho) {
        this.thanhpho = thanhpho;
    }
}
