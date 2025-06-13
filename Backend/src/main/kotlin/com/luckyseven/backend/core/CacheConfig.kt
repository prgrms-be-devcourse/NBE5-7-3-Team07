package com.luckyseven.backend.core

import com.github.benmanes.caffeine.cache.Caffeine
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.concurrent.TimeUnit

@Configuration
@EnableCaching
class CacheConfig {
    @Bean
    fun cacheManager(): CacheManager = CustomCaffeineCacheManager().apply {
        setCacheConfig(
            "recentExpenses", Caffeine.newBuilder()
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .maximumSize(10_000)
                .recordStats()
        )

        setCacheConfig(
            "teamDashboards", Caffeine.newBuilder()
                .expireAfterWrite(2, TimeUnit.HOURS)
                .maximumSize(5_000)
                .recordStats()
        )

        setCaffeine(
            Caffeine.newBuilder()
                .expireAfterWrite(1, TimeUnit.HOURS) // 기본값
                .maximumSize(2_000)
                .recordStats()
        )
    }
}
