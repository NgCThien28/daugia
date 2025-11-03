package com.example.daugia.dto.request;

import java.util.List;

public class SanPhamCreationRequest {
    private String masp;
    private String madm;
    private String makh;
    private String tensp;
    private String matp;
    private String tinhtrangsp;
    private List<String> hinhanh;

    public String getMasp() {
        return masp;
    }

    public void setMasp(String masp) {
        this.masp = masp;
    }

    public String getMadm() {
        return madm;
    }

    public void setMadm(String madm) {
        this.madm = madm;
    }

    public String getMakh() {
        return makh;
    }

    public void setMakh(String makh) {
        this.makh = makh;
    }

    public String getTensp() {
        return tensp;
    }

    public void setTensp(String tensp) {
        this.tensp = tensp;
    }

    public String getTinhtrangsp() {
        return tinhtrangsp;
    }

    public void setTinhtrangsp(String tinhtrangsp) {
        this.tinhtrangsp = tinhtrangsp;
    }

    public List<String> getHinhanh() {
        return hinhanh;
    }

    public void setHinhanh(List<String> hinhanh) {
        this.hinhanh = hinhanh;
    }

    public String getMatp() {
        return matp;
    }

    public void setMatp(String matp) {
        this.matp = matp;
    }
}
