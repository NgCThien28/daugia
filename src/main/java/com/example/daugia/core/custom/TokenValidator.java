package com.example.daugia.core.custom;

import com.example.daugia.exception.UnauthorizedException;
import com.example.daugia.service.BlacklistService;
import com.example.daugia.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TokenValidator {
    @Autowired
    private BlacklistService blacklistService;
    public String extractBearerOrThrow(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Thiếu token");
        }
        return authorizationHeader.substring(7);
    }

    // Kiểm tra blacklist + validate JWT, trả về email
    public String validateAndGetEmailFromToken(String token) {
        if (token == null || token.isBlank()) {
            throw new UnauthorizedException("Thiếu token");
        }
        if (blacklistService.isBlacklisted(token)) {
            throw new UnauthorizedException("Token đã bị vô hiệu hóa");
        }
        String email = JwtUtil.validateToken(token);
        if (email == null) {
            throw new UnauthorizedException("Token không hợp lệ hoặc hết hạn");
        }
        return email;
    }

    // Tiện ích gộp: nhận header Authorization → trả email sau khi xác thực
    public String authenticateAndGetEmail(String authorizationHeader) {
        String token = extractBearerOrThrow(authorizationHeader);
        return validateAndGetEmailFromToken(token);
    }
}
