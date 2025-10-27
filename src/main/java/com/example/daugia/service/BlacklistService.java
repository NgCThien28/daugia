package com.example.daugia.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

@Service
public class BlacklistService {

    // Map lưu token + thời gian hết hạn
    private final ConcurrentHashMap<String, Long> blacklist = new ConcurrentHashMap<>();

    // Thêm token vào danh sách đen
    public void addToken(String token, long expirationTime) {
        blacklist.put(token, expirationTime);
    }

    public boolean isBlacklisted(String token) {
        return blacklist.containsKey(token);
    }

//    // Kiểm tra token có bị blacklist không
//    public boolean isBlacklisted(String token) {
//        Long exp = blacklist.get(token);
//        if (exp == null) return false;
//        if (exp < System.currentTimeMillis()) {
//            blacklist.remove(token); // tự động dọn rác
//            return false;
//        }
//        return true;
//    }
    @Scheduled(fixedRate = 600000)
    public void cleanExpiredTokens() {
        long now = System.currentTimeMillis();
        blacklist.entrySet().removeIf(e -> e.getValue() < now);
    }
}
