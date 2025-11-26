package com.example.daugia.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Setter
@Getter
public class SanPhamCreationRequest {
    private String masp;
    private String madm;
    private String makh;
    private String tensp;
    private String matp;
    private String tinhtrangsp;
    private List<String> hinhanh;
    private BigDecimal giamongdoi;
}
