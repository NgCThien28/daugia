package com.example.daugia.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Date;

@Getter
@Setter
public class TransactionDTO {
    private Date date;
    private Long count;

    public TransactionDTO(Date date, Long count) {
        this.date = date;
        this.count = count;
    }
}
