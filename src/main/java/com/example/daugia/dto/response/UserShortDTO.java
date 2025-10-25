package com.example.daugia.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class UserShortDTO {
    private String matk;
    private String ho;
    private String tenlot;
    private String ten;
    private String email;
    private String sdt;

    public UserShortDTO(String matk, String ho, String tenlot, String ten, String email, String sdt) {
        this.matk = matk;
        this.ho = ho;
        this.tenlot = tenlot;
        this.ten = ten;
        this.email = email;
        this.sdt = sdt;
    }

    public UserShortDTO(String ho, String tenlot, String ten) {
        this.ho = ho;
        this.tenlot = tenlot;
        this.ten = ten;
    }

    public UserShortDTO(String matk) {
        this.matk = matk;
    }

    public UserShortDTO() {
    }

    public String getMatk() {
        return matk;
    }

    public void setMatk(String matk) {
        this.matk = matk;
    }

    public String getHo() {
        return ho;
    }

    public void setHo(String ho) {
        this.ho = ho;
    }

    public String getTenlot() {
        return tenlot;
    }

    public void setTenlot(String tenlot) {
        this.tenlot = tenlot;
    }

    public String getTen() {
        return ten;
    }

    public void setTen(String ten) {
        this.ten = ten;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSdt() {
        return sdt;
    }

    public void setSdt(String sdt) {
        this.sdt = sdt;
    }
}
