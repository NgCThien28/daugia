package com.example.daugia.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

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

    public String getMatp() {
        return matp;
    }

    public void setMatp(String matp) {
        this.matp = matp;
    }

    public String getTentp() {
        return tentp;
    }

    public void setTentp(String tentp) {
        this.tentp = tentp;
    }
}
