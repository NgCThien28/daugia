package com.example.daugia.dto.request;

import java.sql.Timestamp;

public class ThongBaoCreationRequest {
    private String maqtv;
    private String noidung;
    private Timestamp thoigian;

    public String getMaqtv() {
        return maqtv;
    }

    public void setMaqtv(String maqtv) {
        this.maqtv = maqtv;
    }

    public String getNoidung() {
        return noidung;
    }

    public void setNoidung(String noidung) {
        this.noidung = noidung;
    }

    public Timestamp getThoigian() {
        return thoigian;
    }

    public void setThoigian(Timestamp thoigian) {
        this.thoigian = thoigian;
    }
}
