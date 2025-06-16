package com.luckyseven.backend.domain.team.util

import com.luckyseven.backend.domain.budget.entity.Budget
import com.luckyseven.backend.domain.expense.entity.Expense
import com.luckyseven.backend.domain.expense.repository.CategoryExpenseSum
import com.luckyseven.backend.domain.member.entity.Member
import com.luckyseven.backend.domain.team.dto.*
import com.luckyseven.backend.domain.team.dto.TeamDashboardResponse.CategoryExpenseSumDto
import com.luckyseven.backend.domain.team.dto.TeamDashboardResponse.ExpenseDto
import com.luckyseven.backend.domain.team.entity.Team
import com.luckyseven.backend.domain.team.entity.TeamMember
import java.math.BigDecimal
import java.time.LocalDateTime

object TeamMapper {
    fun toTeamListResponse(team: Team): TeamListResponse {
        return TeamListResponse(
            id = team.id,
            name = team.name,
            teamCode = team.teamCode
        )
    }

    /**
     * TeamCreateRequest와 멤버 정보를 Team 엔티티로 변환한다.
     *
     * @param request  요청
     * @param leader   리더가 될 멤버
     * @param teamCode 팀 join 시 필요한 팀 코드
     * @return Team 엔티티
     */
    fun toTeamEntity(
        request: TeamCreateRequest,
        leader: Member,
        teamCode: String,
    ): Team {
        return Team(
            name = request.name,
            teamCode = teamCode,
            teamPassword = request.teamPassword,
            leader = leader,
        )
    }

    /**
     * Member 와 Team 정보를 TeamMember 엔티티로 변환환다
     *
     * @param member 연결할 멤버
     * @param team   연결할 팀
     * @return TeamMember 엔티티
     */
    fun toTeamMemberEntity(team: Team, member: Member): TeamMember {
        return TeamMember(
            team = team,
            member = member
        )
    }


    /**
     * Team 엔티티를 TeamCreateResponse로 변환합니다.
     *
     * @param team 변환할 팀 엔티티
     * @return 변환된 팀 생성 응답 DTO
     */
    fun toTeamCreateResponse(team: Team): TeamCreateResponse {
        return team.let {
            TeamCreateResponse(
                id = it.id,
                name = it.name,
                teamCode = it.teamCode,
                leaderId = it.leader.id
            )
        }
    }

    /**
     * Team 엔티티를 TeamJoinResponse로 변환합니다.
     *
     * @param team 변환할 팀 엔티티
     * @return 변환된 팀 참가 응답 DTO
     */
    fun toTeamJoinResponse(team: Team): TeamJoinResponse {
        return team.let {
            TeamJoinResponse(
                id = it.id,
                teamName = it.name,
                teamCode = it.teamCode,
                leaderId = it.leader.id
            )
        }
    }

    /**
     * Team, Budget과 Expense 목록을 TeamDashboardResponse로 변환합니다.
     *
     * @param team     변환할 팀 엔티티
     * @param budget   팀의 예산 정보
     * @param expenses 팀의 지출 목록
     * @return 변환된 팀 대시보드 응답 DTO
     */
    fun toTeamDashboardResponse(
        team: Team?,
        budget: Budget?,
        expenses: List<Expense>?,
        categoryExpenseSums: List<CategoryExpenseSum>?
    ): TeamDashboardResponse? {
        if (team == null) return null

        val expenseDtoList = expenses?.map { expense ->
            ExpenseDto(
                id = expense.id,
                description = expense.description,
                amount = expense.amount,
                paymentMethod = expense.paymentMethod,
                category = expense.category,
                date = expense.createdAt ?: LocalDateTime.now(),
                payerNickname = expense.payer.nickname
            )
        } ?: emptyList()

        val categorySumDtos = categoryExpenseSums?.map { sum ->
            CategoryExpenseSumDto(
                category = sum.category,
                totalAmount = sum.totalAmount
            )
        } ?: emptyList()

        return TeamDashboardResponse(
            teamId = team.id,
            teamCode = team.teamCode,
            teamName = team.name,
            teamPassword = team.teamPassword,
            foreignCurrency = budget?.foreignCurrency,
            balance = budget?.balance,
            foreignBalance = budget?.foreignBalance ?: BigDecimal.ZERO,
            totalAmount = budget?.totalAmount,
            avgExchangeRate = budget?.avgExchangeRate ?: BigDecimal.ZERO,
            updatedAt = budget?.updatedAt ?: LocalDateTime.now(),
            expenseList = expenseDtoList,
            categoryExpenseSumList = categorySumDtos
        )
    }
}
