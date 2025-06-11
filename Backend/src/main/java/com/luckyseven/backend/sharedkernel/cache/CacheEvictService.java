package com.luckyseven.backend.sharedkernel.cache;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class CacheEvictService {

  private final CacheManager cacheManager;

  public void evictByPrefix(String cacheName, String prefix) {
    Cache cache = cacheManager.getCache(cacheName);
    if (cache == null) {
      log.warn("캐시 '{}'를 찾을 수 없습니다. 무효화 작업을 건너뜁니다.", cacheName);
      return;
    }

    if (cache instanceof CaffeineCache caffeineCache) {
      com.github.benmanes.caffeine.cache.Cache<Object, Object> nativeCache =
          caffeineCache.getNativeCache();

      List<String> keysToRemove = nativeCache.asMap().keySet().stream()
          .map(Object::toString)
          .filter(key -> key.startsWith(prefix))
          .toList();
      keysToRemove.forEach(nativeCache::invalidate);

      log.info("Caffeine 캐시 '{}'에서 '{}'로 시작하는 {}건 무효화 완료.",
          cacheName, prefix, keysToRemove.size());
      return;
    }

    cache.clear();
    log.info("Caffeine 외 캐시 구현체라 전체 clear() 호출 – 네임스페이스='{}'", cacheName);
  }
}
