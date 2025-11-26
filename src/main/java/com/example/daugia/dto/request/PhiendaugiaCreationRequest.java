package com.example.daugia.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Setter
@Getter
public class PhiendaugiaCreationRequest {
    private String masp;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Timestamp thoigianbd;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Timestamp thoigiankt;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Timestamp thoigianbddk;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Timestamp thoigianktdk;
    private BigDecimal giakhoidiem;
    private BigDecimal buocgia;
    private BigDecimal tiencoc;

}
