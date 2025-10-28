package com.example.daugia.config;

import com.example.daugia.service.BlacklistService;
import com.example.daugia.util.JwtUtil;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class JwtFilter implements Filter {

    @Autowired
    private BlacklistService blacklistService;

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        String path = request.getRequestURI();

        // Bỏ qua các endpoint public
        if (path.startsWith("/api")) {
            chain.doFilter(req, res);
            return;
        }

        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Thiếu token xác thực");
            return;
        }

        String token = header.substring(7);

        // Kiểm tra nếu token bị blacklist
        if (blacklistService.isBlacklisted(token)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Token đã bị vô hiệu (đăng xuất)");
            return;
        }

        String username = JwtUtil.validateToken(token);
        if (username == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Token không hợp lệ hoặc hết hạn");
            return;
        }

        request.setAttribute("username", username);
        chain.doFilter(req, res);
    }
}