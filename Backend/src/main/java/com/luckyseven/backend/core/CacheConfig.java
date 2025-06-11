package com.luckyseven.backend.core;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig {

  @Bean
  public CacheManager cacheManager() {
    CustomCaffeineCacheManager cm = new CustomCaffeineCacheManager();
    cm.setCacheConfig("recentExpenses", Caffeine.newBuilder()
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .maximumSize(10_000)
            .recordStats());

    cm.setCacheConfig("teamDashboards", Caffeine.newBuilder()
            .expireAfterWrite(2, TimeUnit.HOURS)
            .maximumSize(5_000)
            .recordStats());

    cm.setCaffeine(Caffeine.newBuilder()
            .expireAfterWrite(1, TimeUnit.HOURS)  // 기본값
            .maximumSize(2_000)
            .recordStats()
    );

    return cm;
  }
}
