package com.example.daugia.config;

import com.example.daugia.core.ws.HistoryCache;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppBeans {
    @Bean
    public HistoryCache historyCache() {
        //Luu 20 ban ghi gan nhat moi phien
        return new HistoryCache(20);
    }
}