package com.example.daugia.service;

import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

@Service
public class ActiveTokenService {

    // Map email → token hiện tại
    private final ConcurrentHashMap<String, String> activeTokens = new ConcurrentHashMap<>();

    public void saveActiveToken(String email, String token) {
        activeTokens.put(email, token);
    }

    public String getActiveToken(String email) {
        return activeTokens.get(email);
    }

    public void removeActiveToken(String email) {
        activeTokens.remove(email);
    }

    public boolean isSameToken(String email, String token) {
        String active = activeTokens.get(email);
        return active != null && active.equals(token);
    }
}
