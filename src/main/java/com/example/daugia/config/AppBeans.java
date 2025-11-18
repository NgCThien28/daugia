package com.example.daugia.config;

import com.example.daugia.core.ws.HistoryCache;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppBeans {
    @Bean
    public HistoryCache historyCache() {
        // Lưu 20 bản ghi gần nhất mỗi phiên
        return new HistoryCache(20);
    }
}