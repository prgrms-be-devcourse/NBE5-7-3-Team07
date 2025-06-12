package com.luckyseven.backend.domain.budget.service

import com.luckyseven.backend.domain.budget.dao.BudgetRepository
import com.luckyseven.backend.domain.budget.dto.*
import com.luckyseven.backend.domain.budget.entity.Budget
import com.luckyseven.backend.domain.budget.mapper.BudgetMapper
import com.luckyseven.backend.domain.budget.validator.BudgetValidator
import com.luckyseven.backend.domain.expense.repository.ExpenseRepository
import com.luckyseven.backend.domain.team.entity.Team
import com.luckyseven.backend.domain.team.repository.TeamRepository
import com.luckyseven.backend.sharedkernel.exception.CustomLogicException
import com.luckyseven.backend.sharedkernel.exception.ExceptionCode
import jakarta.persistence.EntityNotFoundException
import lombok.RequiredArgsConstructor
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.function.Supplier

@Service
class BudgetService(
    private val teamRepository: TeamRepository,
    private val expenseRepository: ExpenseRepository,
    private val budgetRepository: BudgetRepository,
    private val budgetMapper: BudgetMapper,
    private val budgetValidator: BudgetValidator,
) {

    @Transactional
    fun save(
        teamId: Long,
        loginMemberId: Long,
        request: BudgetCreateRequest
    ): BudgetCreateResponse {
        budgetValidator.validateBudgetNotExist(teamId)
        val team = teamRepository.findById(teamId)
            .orElseThrow { EntityNotFoundException("팀을 찾을 수 없습니다: " + teamId) }

        val budget = budgetMapper.toEntity(team, loginMemberId, request)

        budget.setExchangeInfo(
            request.isExchanged,
            budget.totalAmount,
            request.exchangeRate
        )

        budgetRepository.save(budget)
        team.setBudget(budget)

        return budgetMapper.toCreateResponse(budget)
    }

    @Transactional(readOnly = true)
    fun getByTeamId(teamId: Long): BudgetReadResponse {
        val budget = budgetValidator.validateBudgetExist(teamId)

        return budgetMapper.toReadResponse(budget)
    }

    @Transactional
    fun updateByTeamId(
        teamId: Long,
        loginMemberId: Long,
        request: BudgetUpdateRequest
    ): BudgetUpdateResponse {
        val budget = budgetValidator.validateBudgetExist(teamId)

        budget.setBy = loginMemberId
        updateTotalAmountOrExchangeRate(request, budget)

        return budgetMapper.toUpdateResponse(budget)
    }

    @Transactional
    fun addBudgetByTeamId(
        teamId: Long,
        loginMemberId: Long,
        request: BudgetAddRequest
    ): BudgetUpdateResponse {
        val budget = budgetValidator.validateBudgetExist(teamId)

        budget.setBy = loginMemberId
        addBudget(request, budget)

        return budgetMapper.toUpdateResponse(budget)
    }

    @Transactional
    fun deleteByTeamId(teamId: Long) {
        val team = teamRepository.findById(teamId)
            .orElseThrow { EntityNotFoundException("팀을 찾을 수 없습니다: " + teamId) }

        if (expenseRepository.existsByTeamId(teamId)) {
            throw CustomLogicException(ExceptionCode.EXIST_EXPENSE)
        }

        val budget = budgetValidator.validateBudgetExist(teamId)

        team.setBudget(null)
        teamRepository.save(team)
        budgetRepository.delete(budget)
    }

    companion object {
        private fun addBudget(request: BudgetAddRequest, budget: Budget) {
            // totalAmount, Balance += additionalBudget
            budget.updateExchangeInfo(
                request.isExchanged,
                request.additionalBudget,
                request.exchangeRate
            )
            val sum = budget.totalAmount.add(request.additionalBudget)
            budget.setTotalAmount(sum)
        }

        private fun updateTotalAmountOrExchangeRate(request: BudgetUpdateRequest, budget: Budget) {
            // totalAmount, Balance update
            budget.setTotalAmount(request.totalAmount)

            // avgExchange, foreignBalance update
            budget.setExchangeInfo(
                request.isExchanged,
                budget.totalAmount,
                request.exchangeRate
            )

            // totalAmount만 수정을 원할 경우, foreignBalance update
            budget.setForeignBalance()
        }
    }
}
