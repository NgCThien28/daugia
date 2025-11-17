package com.example.daugia.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
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

}
