package com.example.daugia.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse <T> {
    private int code;
    private String message;
    private T result;

    public static <T> ApiResponse<T> success(T data, String message) {
        ApiResponse<T> res = new ApiResponse<>();
        res.setCode(200);
        res.setMessage(message);
        res.setResult(data);
        return res;
    }

    public static <T> ApiResponse<T> error(int code, String message) {
        ApiResponse<T> res = new ApiResponse<>();
        res.setCode(code);
        res.setMessage(message);
        res.setResult(null);
        return res;
    }

}
