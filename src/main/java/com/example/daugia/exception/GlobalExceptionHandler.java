package com.example.daugia.exception;

import com.example.daugia.dto.request.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final MediaType SSE = MediaType.TEXT_EVENT_STREAM;
    private final ObjectMapper mapper = new ObjectMapper();

    @ExceptionHandler({
            NotFoundException.class,
            ConflictException.class,
            ValidationException.class,
            UnauthorizedException.class,
            ForbiddenException.class,
            StorageException.class,
            IllegalArgumentException.class,
            Exception.class
    })
    public ResponseEntity<?> handleAll(Exception ex, HttpServletRequest request) {
        var resolved = resolve(ex);
        HttpStatus status = resolved.status;
        int code = resolved.code;
        String message = resolved.message;

        if (isSse(request)) {
            String sseBody = buildSseErrorBody(code, message);
            return ResponseEntity.status(status)
                    .contentType(SSE)
                    .body(sseBody);
        }

        ApiResponse<?> body = ApiResponse.error(code, message);
        return ResponseEntity.status(status)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body);
    }

    private boolean isSse(HttpServletRequest req) {
        String accept = req.getHeader(HttpHeaders.ACCEPT);
        return accept != null && accept.contains(MediaType.TEXT_EVENT_STREAM_VALUE);
    }

    private String buildSseErrorBody(int code, String message) {
        try {
            String json = mapper.writeValueAsString(ApiResponse.error(code, message));
            return "event: error\n" + "data: " + json + "\n\n";
        } catch (Exception e) {
            return "event: error\n" +
                    "data: {\"code\":" + code + ",\"message\":\"" + safe(message) + "\"}\n\n";
        }
    }

    private String safe(String s) {
        if (s == null) return "";
        return s.replace("\n", " ").replace("\r", " ").replace("\"", "\\\"");
    }

    private Resolved resolve(Exception ex) {
        String msg = ex.getMessage() == null ? "" : ex.getMessage();

        if (ex instanceof NotFoundException)
            return new Resolved(HttpStatus.NOT_FOUND, 404, msg);
        if (ex instanceof ConflictException)
            return new Resolved(HttpStatus.CONFLICT, 409, msg);
        if (ex instanceof ValidationException)
            return new Resolved(HttpStatus.BAD_REQUEST, 400, msg);
        if (ex instanceof UnauthorizedException)
            return new Resolved(HttpStatus.UNAUTHORIZED, 401, msg);
        if (ex instanceof ForbiddenException)
            return new Resolved(HttpStatus.FORBIDDEN, 403, msg);
        if (ex instanceof StorageException)
            return new Resolved(HttpStatus.INTERNAL_SERVER_ERROR, 500, msg);
        if (ex instanceof IllegalArgumentException)
            return new Resolved(HttpStatus.BAD_REQUEST, 400, msg);

        return new Resolved(HttpStatus.INTERNAL_SERVER_ERROR, 500, "Lỗi hệ thống: " + msg);
    }

    private record Resolved(HttpStatus status, int code, String message) {
    }
}