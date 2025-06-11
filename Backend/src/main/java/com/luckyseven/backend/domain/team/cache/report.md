
# TeamDashboard 캐싱 전략 분석

TeamDashboard에 대한 캐싱 전략을 분석한 결과, 현재 구현은 적절하게 설계되어 있습니다. 다음과 같은 주요 특징을 가지고 있습니다:

## 캐싱 구현 구조

1. **Write-through 캐싱 패턴 사용**:
    - `TeamDashboardCacheService`에서 명시적으로 "지출 기반 Write-through 캐싱 구조"를 구현
    - 데이터가 변경될 때 캐시와 데이터베이스를 동시에 업데이트하는 방식

2. **캐시 무효화 메커니즘**:
    - Budget 엔티티의 `updatedAt` 필드를 캐시 무효화 키로 사용
    - `BaseEntity`에서 상속받은 `updatedAt` 필드는 JPA의 `@LastModifiedDate` 어노테이션으로 자동 업데이트됨
    - 캐시된 대시보드의 `updatedAt`과 Budget의 `updatedAt`을 비교하여 캐시 유효성 검증

3. **성능 모니터링**:
    - Micrometer를 사용하여 캐시 히트/미스 카운터 구현
    - 캐시 효율성을 모니터링할 수 있는 메트릭 제공

## 캐싱 로직 흐름

`TeamService.getTeamDashboard()` 메서드의 캐싱 로직:

1. 캐시에서 대시보드 데이터 조회 시도
2. 캐시된 데이터가 있으면:
    - Budget의 최신 `updatedAt` 조회
    - 캐시된 대시보드의 `updatedAt`과 비교
    - 일치하면 캐시된 데이터 반환
3. 캐시된 데이터가 없거나 `updatedAt`이 다르면:
    - `refreshTeamDashboard()` 호출하여 최신 데이터 조회
    - 새로운 대시보드 데이터 생성 및 캐시 갱신

## 캐시 갱신 로직

`refreshTeamDashboard()` 메서드:
1. 팀, 예산, 최근 지출, 카테고리별 지출 합계 등 필요한 모든 데이터 조회
2. `TeamMapper.toTeamDashboardResponse()`를 통해 응답 객체 생성
3. Budget의 `updatedAt`을 TeamDashboardResponse에 포함
4. 생성된 응답 객체를 캐시에 저장

## 결론

현재 TeamDashboard의 캐싱 전략은 다음과 같은 이유로 적절합니다:

1. **효율적인 캐시 무효화**: Budget 엔티티의 변경을 감지하여 필요할 때만 캐시를 갱신
2. **일관성 유지**: 캐시와 데이터베이스 간의 데이터 일관성 보장
3. **성능 최적화**: 불필요한 데이터베이스 쿼리 감소
4. **모니터링 가능**: 캐시 성능 지표 제공

이 캐싱 전략은 특히 대시보드와 같이 읽기 작업이 많고 계산 비용이 높은 기능에 적합하며, 시스템의 전반적인 성능을 향상시키는 데 기여합니다.