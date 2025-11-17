package com.example.daugia.controller;

import com.example.daugia.core.custom.TokenValidator;
import com.example.daugia.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/notifications")
@CrossOrigin(origins = "http://localhost:5173")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;
    @Autowired
    private TokenValidator tokenValidator;

    @GetMapping(value = "/connect", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter connect(@RequestParam("token") String token) {
        String email = tokenValidator.validateAndGetEmailFromToken(token);
        return notificationService.createEmitter(email);
    }

}
