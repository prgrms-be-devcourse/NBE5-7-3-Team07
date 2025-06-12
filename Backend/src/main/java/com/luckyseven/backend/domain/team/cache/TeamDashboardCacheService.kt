package com.luckyseven.backend.domain.team.cache;

import com.luckyseven.backend.domain.team.dto.TeamDashboardResponse;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

/**
 * 팀 대시보드 캐싱 서비스 - 지출 기반 Write-through 캐싱 구조 구현 - 지출 변경 시에만 집계 쿼리를 실행하고 캐시에 저장 - 대시보드 조회 시에는 항상 캐시된
 * 값만 반환
 */
@Service
@RequiredArgsConstructor
public class TeamDashboardCacheService {

  private final CacheManager cacheManager;
  private static final String CACHE_NAME = "teamDashboards";
  private static final String KEY_PREFIX = "team:dashboard:";

  private final MeterRegistry meterRegistry;
  private Counter cacheHitCounter;
  private Counter cacheMissCounter;

  @PostConstruct
  private void initCounters() {
    cacheHitCounter = meterRegistry.counter("team_dashboard_cache_hit_total");
    cacheMissCounter = meterRegistry.counter("team_dashboard_cache_miss_total");
  }

  /**
   * 팀 대시보드 데이터를 캐시에 저장
   *
   * @param teamId    팀 ID
   * @param dashboard 대시보드 데이터
   */
  public void cacheTeamDashboard(Long teamId, TeamDashboardResponse dashboard) {
    Cache cache = cacheManager.getCache(CACHE_NAME);
    if (cache != null) {
      String key = KEY_PREFIX + teamId;
      cache.put(key, dashboard);
    }
  }

  /**
   * 캐시된 팀 대시보드 데이터 조회
   *
   * @param teamId 팀 ID
   * @return 캐시된 대시보드 데이터, 없으면 null 반환
   */
  public TeamDashboardResponse getCachedTeamDashboard(Long teamId) {
    Cache cache = cacheManager.getCache(CACHE_NAME);
    if (cache != null) {
      String key = KEY_PREFIX + teamId;
      Cache.ValueWrapper valueWrapper = cache.get(key);
      if (valueWrapper != null) {
        cacheHitCounter.increment();
        return (TeamDashboardResponse) valueWrapper.get();
      }
    }
    cacheMissCounter.increment();
    return null;
  }


  /**
   * 팀 대시보드 캐시 삭제
   *
   * @param teamId 팀 ID
   */
  public void evictTeamDashboardCache(Long teamId) {
    Cache cache = cacheManager.getCache(CACHE_NAME);
    if (cache != null) {
      String key = KEY_PREFIX + teamId;
      cache.evict(key);
    }
  }
}
