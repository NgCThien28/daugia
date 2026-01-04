package com.example.daugia.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Date;

@Getter
@Setter
public class TransactionAmountDTO {
    private Date date;
    private BigDecimal total;

    public TransactionAmountDTO(Date date, BigDecimal total) {
        this.date = date;
        this.total = total;
    }
}
