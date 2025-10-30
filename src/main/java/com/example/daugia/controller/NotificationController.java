package com.example.daugia.controller;

import com.example.daugia.service.NotificationService;
import com.example.daugia.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/notifications")
@CrossOrigin(origins = "http://localhost:5173")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @GetMapping("/connect")
    public SseEmitter connect(@RequestParam("token") String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("Thiếu token");
        }
        String email = JwtUtil.validateToken(token);
        if (email == null) {
            throw new IllegalArgumentException("Token không hợp lệ");
        }

        return notificationService.createEmitter(email);
    }

}
