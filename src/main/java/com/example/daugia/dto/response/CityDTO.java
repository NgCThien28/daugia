package com.example.daugia.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class CityDTO {
    private String matp;
    private String tentp;

    public CityDTO(String matp, String tentp) {
        this.matp = matp;
        this.tentp = tentp;
    }

    public CityDTO(String tentp) {
        this.tentp = tentp;
    }

    public CityDTO() {
    }

}
