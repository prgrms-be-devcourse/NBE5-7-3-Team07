package com.luckyseven.backend.domain.budget.mapper

import com.luckyseven.backend.domain.budget.dto.BudgetCreateRequest
import com.luckyseven.backend.domain.budget.dto.BudgetCreateResponse
import com.luckyseven.backend.domain.budget.dto.BudgetReadResponse
import com.luckyseven.backend.domain.budget.dto.BudgetUpdateResponse
import com.luckyseven.backend.domain.budget.entity.Budget
import com.luckyseven.backend.domain.team.entity.Team
import org.springframework.stereotype.Component

@Component
class BudgetMapper {
    companion object {
        private const val ID_ERROR_MSG = "Budget ID는 null이 아니어야 합니다."
        private const val CREATED_AT_ERROR_MSG = "Budget createdAt은 null이 아니어야 합니다."
        private const val UPDATED_AT_ERROR_MSG = "Budget updatedAt은 null이 아니어야 합니다."
    }

    fun toEntity(team: Team, loginMemberId: Long, request: BudgetCreateRequest): Budget {
        return Budget(
            team = team,
            totalAmount = request.totalAmount,
            avgExchangeRate = request.exchangeRate,
            setBy = loginMemberId,
            balance = request.totalAmount,
            foreignCurrency = request.foreignCurrency
        )
    }

    fun toCreateResponse(budget: Budget): BudgetCreateResponse {
        return BudgetCreateResponse(
            requireNotNull(budget.id) { ID_ERROR_MSG },
            requireNotNull(budget.createdAt) { CREATED_AT_ERROR_MSG },
            budget.setBy,
            budget.balance,
            budget.avgExchangeRate,
            budget.foreignBalance
        )
    }

    fun toReadResponse(budget: Budget): BudgetReadResponse {
        return BudgetReadResponse(
            requireNotNull(budget.id) { ID_ERROR_MSG },
            requireNotNull(budget.updatedAt) { UPDATED_AT_ERROR_MSG },
            budget.setBy,
            budget.totalAmount,
            budget.balance,
            budget.foreignCurrency,
            budget.avgExchangeRate,
            budget.foreignBalance
        )
    }

    fun toUpdateResponse(budget: Budget): BudgetUpdateResponse {
        return BudgetUpdateResponse(
            requireNotNull(budget.id) { ID_ERROR_MSG },
            requireNotNull(budget.updatedAt) { UPDATED_AT_ERROR_MSG },
            budget.setBy,
            budget.balance,
            budget.foreignCurrency,
            budget.avgExchangeRate,
            budget.foreignBalance
        )
    }
}