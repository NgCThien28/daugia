package com.example.daugia.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Setter
@Getter
public class ThongBaoCreationRequest {
    private String maqtv;
    private String noidung;
    private Timestamp thoigian;

}
