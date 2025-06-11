package com.luckyseven.backend.core;

import com.github.benmanes.caffeine.cache.Caffeine;
import jakarta.validation.constraints.NotNull;
import org.springframework.cache.Cache;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.caffeine.CaffeineCacheManager;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class CustomCaffeineCacheManager extends CaffeineCacheManager {

  private final Map<String, Caffeine<Object, Object>> cacheConfigs = new ConcurrentHashMap<>();

  public void setCacheConfig(String cacheName, Caffeine<Object, Object> cacheConfig) {
    cacheConfigs.put(cacheName, cacheConfig);
  }

  @Override
  protected Cache createCaffeineCache(String name) {
    // (1) cacheConfigs에서 해당 캐시 이름의 설정을 꺼낸다
    Caffeine<Object, Object> cacheConfig = cacheConfigs.get(name);

    // (2) 있으면 그걸로 CaffeineCache 생성
    if (cacheConfig != null) {
      return new CaffeineCache(name, cacheConfig.build(), isAllowNullValues());
    }

    // (3) 없으면 부모 클래스 기본 방식으로 생성
    return super.createCaffeineCache(name);
  }
}
