package com.example.daugia.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class ImageDTO {
    private String maanh;
    private String tenanh;

    public ImageDTO(String maanh, String tenanh) {
        this.maanh = maanh;
        this.tenanh = tenanh;
    }

    public ImageDTO() {
    }

    public String getMaanh() {
        return maanh;
    }

    public void setMaanh(String maanh) {
        this.maanh = maanh;
    }

    public String getTenanh() {
        return tenanh;
    }

    public void setTenanh(String tenanh) {
        this.tenanh = tenanh;
    }
}
