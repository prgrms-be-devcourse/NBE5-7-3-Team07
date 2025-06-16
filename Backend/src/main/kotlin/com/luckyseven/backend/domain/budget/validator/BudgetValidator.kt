package com.luckyseven.backend.domain.budget.validator

import com.luckyseven.backend.domain.budget.dao.BudgetRepository
import com.luckyseven.backend.domain.budget.dto.BudgetAddRequest
import com.luckyseven.backend.domain.budget.dto.BudgetCreateRequest
import com.luckyseven.backend.domain.budget.dto.BudgetUpdateRequest
import com.luckyseven.backend.domain.budget.entity.Budget
import com.luckyseven.backend.sharedkernel.exception.CustomLogicException
import com.luckyseven.backend.sharedkernel.exception.ExceptionCode
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
class BudgetValidator(
    private val budgetRepository: BudgetRepository
) {

    fun validateBudgetNotExist(teamId: Long) {
        val budgetOptional: Budget? = budgetRepository.findByTeamId(teamId)

        budgetOptional?.let {
            throw CustomLogicException(
                ExceptionCode.BUDGET_CONFLICT,
                "budgetId: " + budgetOptional.id
            )
        }
    }

    fun validateBudgetExist(teamId: Long): Budget {
        return budgetRepository.findByTeamId(teamId)
            ?: throw CustomLogicException(ExceptionCode.TEAM_NOT_FOUND, "teamId: $teamId")
    }

    fun validateRequest(request: BudgetCreateRequest) {
        validateIsExchangedRequest(request.isExchanged, request.exchangeRate)
    }

    fun validateRequest(request: BudgetUpdateRequest) {
        validateIsExchangedRequest(request.isExchanged, request.exchangeRate)
    }

    fun validateRequest(request: BudgetAddRequest) {
        validateIsExchangedRequest(request.isExchanged, request.exchangeRate)
        // additionalAmount 입력 시 isExchanged 필수
    }

    private fun validateIsExchangedRequest(isExchanged: Boolean, exchangeRate: BigDecimal?) {
        if (isExchanged && exchangeRate == null) {
            throw CustomLogicException(ExceptionCode.BAD_REQUEST, "환전 여부가 true인데 환율이 없습니다.")
        }
        if (!isExchanged && exchangeRate != null) {
            throw CustomLogicException(ExceptionCode.BAD_REQUEST, "환전 여부가 false인데 환율이 있습니다.")
        }
    }
}