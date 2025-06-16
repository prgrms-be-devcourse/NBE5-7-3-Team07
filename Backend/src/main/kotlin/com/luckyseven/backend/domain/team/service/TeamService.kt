package com.luckyseven.backend.domain.team.service

import com.luckyseven.backend.domain.budget.dao.BudgetRepository
import com.luckyseven.backend.domain.expense.repository.ExpenseRepository
import com.luckyseven.backend.domain.member.repository.MemberRepository
import com.luckyseven.backend.domain.member.service.utill.MemberDetails
import com.luckyseven.backend.domain.team.cache.TeamDashboardCacheService
import com.luckyseven.backend.domain.team.dto.*
import com.luckyseven.backend.domain.team.entity.Team
import com.luckyseven.backend.domain.team.entity.TeamMember
import com.luckyseven.backend.domain.team.repository.TeamMemberRepository
import com.luckyseven.backend.domain.team.repository.TeamRepository
import com.luckyseven.backend.domain.team.util.TeamMapper
import com.luckyseven.backend.sharedkernel.exception.CustomLogicException
import com.luckyseven.backend.sharedkernel.exception.ExceptionCode
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class TeamService(
    val teamRepository: TeamRepository,
    val teamMemberRepository: TeamMemberRepository,
    val memberRepository: MemberRepository,
    val budgetRepository: BudgetRepository,
    val expenseRepository: ExpenseRepository,
    val passwordEncoder: BCryptPasswordEncoder,
    val teamDashboardCacheService: TeamDashboardCacheService
) {


    /**
     * 팀을 생성한다. 생성한 회원을 팀 리더로 등록한다
     *
     * @param request 팀 생성 요청
     * @return 생성된 팀 정보
     */
    @Transactional
    fun createTeam(
        memberDetails: MemberDetails,
        request: TeamCreateRequest
    ): TeamCreateResponse {
        val memberId = memberDetails.id
        val creator = memberRepository.findById(memberId).orElseThrow {
            CustomLogicException(
                ExceptionCode.MEMBER_ID_NOTFOUND,
                memberId
            )
        }

        val teamCode = generateTeamCode()
        val team = TeamMapper.toTeamEntity(request, creator, teamCode)

        creator.addLeadingTeam(team)

        val savedTeam = teamRepository.save<Team>(team)
        val teamMember = TeamMapper.toTeamMemberEntity(savedTeam, creator)

        // 리더를 TeamMember 에 추가
        teamMemberRepository.save<TeamMember>(teamMember)

        savedTeam.addTeamMember(teamMember)
        return TeamMapper.toTeamCreateResponse(savedTeam)
    }

    /**
     * 멤버가 팀 코드와 팀 pwd를 입력하여 팀에 가입한다.
     *
     * @param teamCode     팀 코드
     * @param teamPassword 팀 pwd
     * @return 가입된 팀의 정보
     * @throws IllegalArgumentException 비밀번호 일치 실패 에러.
     */
    @Transactional
    fun joinTeam(
        memberDetails: MemberDetails, teamCode: String,
        teamPassword: String?
    ): TeamJoinResponse {
        val memberId = memberDetails.id
        val member = memberRepository.findById(memberId).orElseThrow {
            CustomLogicException(
                ExceptionCode.MEMBER_ID_NOTFOUND,
                memberId
            )
        }


        val team = teamRepository.findByTeamCode(teamCode) ?: throw CustomLogicException(
            ExceptionCode.TEAM_NOT_FOUND,
            "팀 코드가 [%s]인 팀을 찾을 수 없습니다", teamCode
        )


        if (team.teamPassword != teamPassword){
            throw CustomLogicException(ExceptionCode.TEAM_PASSWORD_MISMATCH)
        }

        val isAlreadyJoined = teamMemberRepository.existsByTeamAndMember(team, member)
        if (isAlreadyJoined) {
            throw CustomLogicException(
                ExceptionCode.ALREADY_TEAM_MEMBER,
                "회원 ID [%d]는 이미 팀 ID [%d]에 가입되어 있습니다", member.id ?: -1L, team.id?: -1L
            )
        }

        val teamMember = TeamMapper.toTeamMemberEntity(team, member)
        val savedTeamMember = teamMemberRepository.save<TeamMember>(teamMember)

        team.addTeamMember(savedTeamMember)
        member.addTeamMember(savedTeamMember)

        if (savedTeamMember.team!!.id != team.id || savedTeamMember.member!!.id != member.id) {
            throw CustomLogicException(
                ExceptionCode.INTERNAL_SERVER_ERROR,
                "팀 멤버 관계 설정에 실패했습니다"
            )
        }

        return TeamMapper.toTeamJoinResponse(team)
    }

    /**
     * 팀 코드를 생성한다
     *
     * @return 생성된 팀 코드
     */
    private fun generateTeamCode(): String = UUID.randomUUID().toString().substring(0, 8)

    @Transactional(readOnly = true)
    fun getTeamsByMemberId(memberId: Long): List<TeamListResponse> {
        memberRepository.findById(memberId)
            .orElseThrow {
                CustomLogicException(
                    ExceptionCode.MEMBER_ID_NOTFOUND,
                    memberId
                )
            }

        val teamMembers: List<TeamMember?> = teamMemberRepository.findByMemberId(memberId)
        return teamMembers.mapNotNull { it?.team }
            .map { TeamMapper.toTeamListResponse(it) }
    }


    /**
     * 팀 대시보드를 조회합니다.
     * 캐시된 데이터가 있고 Budget의 updatedAt과 일치하면 캐시에서 반환,
     * 그렇지 않으면 데이터베이스에서 조회하여 캐시에 저장 후 반환합니다.
     *
     * @param teamId 팀 ID
     * @return 팀 대시보드 응답
     */
    @Transactional(readOnly = true)
    fun getTeamDashboard(teamId: Long): TeamDashboardResponse? {
        // 1. 캐시에서 대시보드 데이터 조회
        val cachedDashboard = teamDashboardCacheService.getCachedTeamDashboard(teamId)

        // 2. 캐시가 있으면 Budget의 updatedAt 확인
        if (cachedDashboard != null) {
            val latestBudgetUpdate = budgetRepository.findUpdatedAtByTeamId(teamId)

            // 3. Budget의 updatedAt이 있고 캐시의 updatedAt과 일치하면 캐시 사용
            if (latestBudgetUpdate != null && cachedDashboard.updatedAt != null &&
                latestBudgetUpdate == cachedDashboard.updatedAt
            ) {
                return cachedDashboard
            }
        }

        // 4. 캐시가 없거나 updatedAt이 다르면 새로 조회하여 캐시 갱신
        return refreshTeamDashboard(teamId)
    }

    /**
     * 팀 대시보드를 새로 조회하여 캐시에 저장합니다.
     *
     * @param teamId 팀 ID
     * @return 팀 대시보드 응답
     */
    @Transactional(readOnly = true)
    fun refreshTeamDashboard(teamId: Long): TeamDashboardResponse? {
        // 팀 및 예산 정보 조회
        val team = teamRepository.findById(teamId)
            .orElseThrow {
                CustomLogicException(
                    ExceptionCode.TEAM_NOT_FOUND,
                    "ID가 [%d]인 팀을 찾을 수 없습니다", teamId
                )
            }

        // 예산 조회 (없으면 null)
        val budget = budgetRepository.findByTeamId(teamId)

        // 최근 지출 내역 조회
        val pageable: Pageable = PageRequest.of(0, 5, Sort.by("createdAt").descending())
        val recentExpenses = expenseRepository.findByTeamId(teamId, pageable).getContent()

        // 카테고리별 지출 합계 조회
        val categoryExpenseSums =
            expenseRepository.findCategoryExpenseSumsByTeamId(teamId)

        // 대시보드 응답 생성
        val dashboard = TeamMapper.toTeamDashboardResponse(
            team, budget, recentExpenses, categoryExpenseSums
        )

        // 캐시에 저장
        teamDashboardCacheService.cacheTeamDashboard(teamId, dashboard)

        return dashboard
    }
}

