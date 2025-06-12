package com.luckyseven.backend.core

import com.github.benmanes.caffeine.cache.Caffeine
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cache.caffeine.CaffeineCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.concurrent.TimeUnit

@Configuration
@EnableCaching
class CacheConfig {

    @Bean
    fun cacheManager(): CacheManager =
        CaffeineCacheManager("recentExpenses").apply {
            setCaffeine(
                Caffeine.newBuilder()
                    .maximumSize(10_000)
                    .expireAfterWrite(5, TimeUnit.MINUTES)
                    .recordStats()
            )
        }
}
