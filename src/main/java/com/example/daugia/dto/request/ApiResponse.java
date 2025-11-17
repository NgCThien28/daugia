package com.example.daugia.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;

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

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getResult() {
        return result;
    }

    public void setResult(T result) {
        this.result = result;
    }
}
