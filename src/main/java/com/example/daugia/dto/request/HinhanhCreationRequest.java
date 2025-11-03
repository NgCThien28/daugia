package com.example.daugia.dto.request;

import java.util.List;

public class HinhanhCreationRequest {
    private String masp;
    private List<String> tenanh;

    public String getMasp() {
        return masp;
    }

    public void setMasp(String masp) {
        this.masp = masp;
    }

    public List<String> getTenanh() {
        return tenanh;
    }

    public void setTenanh(List<String> tenanh) {
        this.tenanh = tenanh;
    }
}
