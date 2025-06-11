package com.luckyseven.backend.core;

import com.github.benmanes.caffeine.cache.Caffeine;
import java.util.concurrent.TimeUnit;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class CacheConfig {

  @Bean
  public CacheManager cacheManager() {
    CaffeineCacheManager cm = new CaffeineCacheManager("recentExpenses");
    cm.setCaffeine(
        Caffeine.newBuilder()
            .maximumSize(10_000)       // 최대 엔트리 수
            .expireAfterWrite(5, TimeUnit.MINUTES) // TTL
            .recordStats()
    );
    return cm;
  }
}
