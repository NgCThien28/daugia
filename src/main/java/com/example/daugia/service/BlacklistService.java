package com.example.daugia.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

@Service
public class BlacklistService {

    // Map luu token va thoi gian het han
    private final ConcurrentHashMap<String, Long> blacklist = new ConcurrentHashMap<>();

    // Them token vao danh sach den
    public void addToken(String token, long expirationTime) {
        blacklist.put(token, expirationTime);
    }

    public boolean isBlacklisted(String token) {
        return blacklist.containsKey(token);
    }

    @Scheduled(fixedRate = 600000)
    public void cleanExpiredTokens() {
        long now = System.currentTimeMillis();
        blacklist.entrySet().removeIf(e -> e.getValue() < now);
    }
}
