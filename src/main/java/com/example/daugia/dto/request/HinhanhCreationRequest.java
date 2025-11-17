package com.example.daugia.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class HinhanhCreationRequest {
    private String masp;
    private List<String> tenanh;

}
