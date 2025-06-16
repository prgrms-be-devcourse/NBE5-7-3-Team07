package com.luckyseven.backend.domain.budget.util

import com.luckyseven.backend.domain.budget.dto.*
import com.luckyseven.backend.domain.budget.entity.Budget
import com.luckyseven.backend.domain.budget.entity.CurrencyCode
import com.luckyseven.backend.domain.member.entity.Member
import com.luckyseven.backend.domain.member.service.utill.MemberDetails
import com.luckyseven.backend.domain.team.entity.Team
import com.luckyseven.backend.domain.team.entity.TeamMember
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import java.math.BigDecimal
import java.time.LocalDateTime

object TestUtils {
    fun genMemberDetails(): MemberDetails {
        return MemberDetails(
            1L,
            "user1111",
            "user1@user.com",
            "user1"
        )
    }

    // 테스트에서 인증된 사용자처럼 보이게 설정
    fun genAuthentication(member: MemberDetails) {
        val auth: Authentication = UsernamePasswordAuthenticationToken(
            member, null, member.getAuthorities()
        )
        SecurityContextHolder.getContext().authentication = auth
    }

    @JvmStatic
    fun genBudgetCreateReq(): BudgetCreateRequest {
        return BudgetCreateRequest(
            BigDecimal.valueOf(100000),
            true,
            BigDecimal.valueOf(1393.7),
            CurrencyCode.USD
        )
    }

    fun genBudgetCreateResp(): BudgetCreateResponse {
        return BudgetCreateResponse(
            1L,
            LocalDateTime.now(),
            1L,
            BigDecimal.valueOf(100000),
            BigDecimal.valueOf(1393.7),
            BigDecimal.valueOf(71.75)
        )
    }

    @JvmStatic
    fun genBudgetUpdateReq(): BudgetUpdateRequest {
        return BudgetUpdateRequest(
            BigDecimal.valueOf(80000),
            true,
            BigDecimal.valueOf(1393.7)
        )
    }

    fun genBudgetUpdateResp(): BudgetUpdateResponse {
        return BudgetUpdateResponse(
            1L,
            LocalDateTime.now(),
            1L,
            BigDecimal.valueOf(80000),
            CurrencyCode.USD,
            BigDecimal.valueOf(1393.7),
            BigDecimal.valueOf(57.40)
        )
    }

    @JvmStatic
    fun genBudgetAddReq(): BudgetAddRequest {
        return BudgetAddRequest(
            BigDecimal.valueOf(50000),
            false,
            null
        )
    }

    fun genBudgetUpdateRespAfterAdd(): BudgetUpdateResponse {
        return BudgetUpdateResponse(
            1L,
            LocalDateTime.now(),
            1L,
            BigDecimal.valueOf(150000),
            CurrencyCode.USD,
            BigDecimal.valueOf(1393.7),
            BigDecimal.valueOf(71.75)
        )
    }

    fun genBudgetReadResp(): BudgetReadResponse {
        return BudgetReadResponse(
            1L,
            LocalDateTime.now(),
            1L,
            BigDecimal.valueOf(100000),
            BigDecimal.valueOf(100000),
            CurrencyCode.USD,
            null,
            null
        )
    }

    fun genMember(): Member {
        return Member(1L, "user1@user.com", "user1111", "user1")
    }

    fun genTeam(): Team {
        val teamMembers = mutableListOf(TeamMember());
        return Team(
            1L,
            "user1",
            "lucky",
            "abcd1234",
            genMember(),
            null,
            teamMembers
        )
    }

    @JvmStatic
    fun genBudget(): Budget {
        return Budget(
            1L,
            genTeam(),
            BigDecimal.valueOf(100000),
            1L,
            BigDecimal.valueOf(100000),
            BigDecimal.valueOf(71.75),
            CurrencyCode.USD,
            BigDecimal.valueOf(1393.7)
        )
    }
}
