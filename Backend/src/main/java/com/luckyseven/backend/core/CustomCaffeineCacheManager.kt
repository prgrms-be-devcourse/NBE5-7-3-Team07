package com.luckyseven.backend.core

import com.github.benmanes.caffeine.cache.Caffeine
import org.springframework.cache.Cache
import org.springframework.cache.caffeine.CaffeineCache
import org.springframework.cache.caffeine.CaffeineCacheManager
import java.util.concurrent.ConcurrentHashMap

class CustomCaffeineCacheManager : CaffeineCacheManager() {
    private val cacheConfigs: MutableMap<String, Caffeine<Any, Any>> = ConcurrentHashMap()

    fun setCacheConfig(cacheName: String, cacheConfig: Caffeine<Any, Any>) {
        cacheConfigs[cacheName] = cacheConfig
    }

    override fun createCaffeineCache(name: String): Cache {
        // (1) cacheConfigs 에서 해당 캐시 이름의 설정을 꺼낸다. key:value
        val cacheConfig = cacheConfigs[name]

        return if (cacheConfig != null) {
            // (2) 있으면 그걸로 CaffeineCache 생성
            CaffeineCache(name, cacheConfig.build<Any, Any>(), isAllowNullValues)
        } else{
            // (3) 없으면 부모 클래스 기본 방식으로 생성
            super.createCaffeineCache(name)
        }

    }
}
