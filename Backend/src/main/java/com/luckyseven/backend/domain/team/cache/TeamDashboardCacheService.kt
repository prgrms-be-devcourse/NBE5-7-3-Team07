package com.luckyseven.backend.domain.team.cache

import com.luckyseven.backend.domain.team.dto.TeamDashboardResponse
import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import jakarta.annotation.PostConstruct
import lombok.RequiredArgsConstructor
import org.springframework.cache.CacheManager
import org.springframework.stereotype.Service

/**
 * 팀 대시보드 캐싱 서비스 - 지출 기반 Write-through 캐싱 구조 구현 - 지출 변경 시에만 집계 쿼리를 실행하고 캐시에 저장 - 대시보드 조회 시에는 항상 캐시된
 * 값만 반환
 */
@Service
@RequiredArgsConstructor
class TeamDashboardCacheService(
    private val cacheManager: CacheManager,
    private val meterRegistry: MeterRegistry,

) {
//    private lateinit var cacheHitCounter: Counter
//    private lateinit var cacheMissCounter: Counter

//    @PostConstruct
//     fun initCounters() {
//        cacheHitCounter = meterRegistry!!.counter("team_dashboard_cache_hit_total")
//        cacheMissCounter = meterRegistry.counter("team_dashboard_cache_miss_total")
//    }

    /**
     * 팀 대시보드 데이터를 캐시에 저장
     *
     * @param teamId    팀 ID
     * @param dashboard 대시보드 데이터
     */
    fun cacheTeamDashboard(teamId: Long, dashboard: TeamDashboardResponse?) {
        cacheManager.getCache(CACHE_NAME).put(KEY_PREFIX + teamId, dashboard)
    }

    /**
     * 캐시된 팀 대시보드 데이터 조회
     *
     * @param teamId 팀 ID
     * @return 캐시된 대시보드 데이터, 없으면 null 반환
     */
    fun getCachedTeamDashboard(teamId: Long?): TeamDashboardResponse? {
        val cache = cacheManager.getCache(CACHE_NAME)
        val key = KEY_PREFIX + teamId
        val valueWrapper = cache?.get(key)
        return if(valueWrapper != null) {
            valueWrapper.get() as? TeamDashboardResponse
        } else{
//            cacheMissCounter.increment()
            null
        }
        return null
    }


    /**
     * 팀 대시보드 캐시 삭제
     *
     * @param teamId 팀 ID
     */
    fun evictTeamDashboardCache(teamId: Long?) {
        cacheManager.getCache(CACHE_NAME)?.evict(KEY_PREFIX + teamId)
    }

    companion object {
        private const val CACHE_NAME = "teamDashboards"
        private const val KEY_PREFIX = "team:dashboard:"
    }
}
