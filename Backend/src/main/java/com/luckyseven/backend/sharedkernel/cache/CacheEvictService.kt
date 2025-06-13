package com.luckyseven.backend.sharedkernel.cache

import org.slf4j.LoggerFactory
import org.springframework.cache.CacheManager
import org.springframework.cache.caffeine.CaffeineCache
import org.springframework.stereotype.Service
import com.github.benmanes.caffeine.cache.Cache as NativeCaffeineCache

@Service
class CacheEvictService(
    private val cacheManager: CacheManager
) {
    private val log = LoggerFactory.getLogger(javaClass)

    fun evictByPrefix(cacheName: String, prefix: String) {
        val cache = cacheManager.getCache(cacheName)
            ?: run {
                log.warn("캐시 '{}'를 찾을 수 없습니다. 무효화 작업을 건너뜁니다.", cacheName)
                return
            }

        if (cache is CaffeineCache) {
            val nativeCache = cache.nativeCache as NativeCaffeineCache<Any, Any>
            val keysToRemove = nativeCache
                .asMap()
                .keys
                .map { it.toString() }
                .filter { it.startsWith(prefix) }

            keysToRemove.forEach { nativeCache.invalidate(it) }
            log.info(
                "Caffeine 캐시 '{}'에서 '{}'로 시작하는 {}건 무효화 완료.",
                cacheName,
                prefix,
                keysToRemove.size
            )
        } else {
            cache.clear()
            log.info("Caffeine 외 캐시 구현체라 전체 clear() 호출 – 네임스페이스='{}'", cacheName)
        }
    }
}
