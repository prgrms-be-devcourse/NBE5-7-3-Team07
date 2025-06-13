package com.luckyseven.backend.domain.expense.service

import com.luckyseven.backend.domain.budget.entity.Budget
import com.luckyseven.backend.domain.expense.dto.*
import com.luckyseven.backend.domain.expense.entity.Expense
import com.luckyseven.backend.domain.expense.enums.PaymentMethod
import com.luckyseven.backend.domain.expense.mapper.ExpenseMapper
import com.luckyseven.backend.domain.expense.repository.ExpenseRepository
import com.luckyseven.backend.domain.member.service.MemberService
import com.luckyseven.backend.domain.settlement.app.SettlementService
import com.luckyseven.backend.domain.team.entity.Team
import com.luckyseven.backend.domain.team.repository.TeamRepository
import com.luckyseven.backend.sharedkernel.cache.CacheEvictService
import com.luckyseven.backend.sharedkernel.dto.PageResponse
import com.luckyseven.backend.sharedkernel.exception.CustomLogicException
import com.luckyseven.backend.sharedkernel.exception.ExceptionCode
import org.springframework.cache.annotation.CacheConfig
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

@Service
@CacheConfig(cacheNames = ["recentExpenses"])
class ExpenseService(

    private val settlementService: SettlementService,
    private val expenseRepository: ExpenseRepository,
    private val teamRepository: TeamRepository,
    private val memberService: MemberService,
    private val cacheEvictService: CacheEvictService
) {

    @Transactional
    fun saveExpense(teamId: Long, request: ExpenseRequest): CreateExpenseResponse {
        val team = findTeamWithBudget(teamId)
        val payer = memberService.findMemberOrThrow(request.payerId)
        val budget = findBudgetOrThrow(team)

        adjustBudgetOnCreate(request.paymentMethod, budget, request.amount)

        val expense = ExpenseMapper.fromExpenseRequest(request, team, payer)
        val savedExpense = expenseRepository.save(expense)

        settlementService.createAllSettlements(request, payer, savedExpense)
        evictCache(teamId)
        return ExpenseMapper.toCreateExpenseResponse(savedExpense, budget)
    }

    @Transactional(readOnly = true)
    fun getExpense(expenseId: Long): ExpenseResponse {
        val expense = findExpenseWithPayer(expenseId)
        return ExpenseMapper.toExpenseResponse(expense)
    }

    @Transactional(readOnly = true)
    @Cacheable(key = "'team:' + #teamId + ':page:' + #pageable.pageNumber + ':size:' + #pageable.pageSize")
    fun getExpenses(teamId: Long, pageable: Pageable): PageResponse<ExpenseResponse> {
        if (!teamRepository.existsById(teamId)) {
            throw CustomLogicException(ExceptionCode.TEAM_NOT_FOUND)
        }
        val page = expenseRepository.findResponsesByTeamId(teamId, pageable)
        return ExpenseMapper.toPageResponse(page)
    }

    @Transactional
    fun updateExpense(expenseId: Long, request: ExpenseUpdateRequest): CreateExpenseResponse {
        val expense = findExpenseWithBudgetOrThrow(expenseId)

        val originalAmount = expense.amount
        val newAmount: BigDecimal = request.amount ?: originalAmount
        val delta = newAmount.subtract(originalAmount)
        val budget = findBudgetOrThrow(expense.team)

        adjustBudgetOnUpdate(delta, expense.paymentMethod, budget)

        expense.update(request.description, newAmount, request.category)
        evictCache(expense.team.id!!)
        return ExpenseMapper.toCreateExpenseResponse(expense, budget)
    }

    @Transactional
    fun deleteExpense(expenseId: Long): ExpenseBalanceResponse {
        val expense = findExpenseWithBudgetOrThrow(expenseId)

        val budget = findBudgetOrThrow(expense.team)
        creditBudgetOnDelete(expense.paymentMethod, budget, expense.amount)

        expenseRepository.delete(expense)
        evictCache(expense.team.id!!)
        return ExpenseMapper.toExpenseBalanceResponse(budget)
    }

    // TODO: Optional 제거, Kotlin Nullable + Elvis 연산자로 변경
    private fun findExpenseWithPayer(expenseId: Long): Expense =
        expenseRepository.findByIdWithPayer(expenseId)
            ?: throw CustomLogicException(ExceptionCode.EXPENSE_NOT_FOUND)

    private fun findBudgetOrThrow(team: Team): Budget =
        team.budget ?: throw CustomLogicException(ExceptionCode.BUDGET_NOT_FOUND)

    private fun findExpenseWithBudgetOrThrow(expenseId: Long): Expense =
        expenseRepository.findWithTeamAndBudgetById(expenseId)
            ?: throw CustomLogicException(ExceptionCode.EXPENSE_NOT_FOUND)

    // Todo: 병합 후 코틀린 스럽게 변경
    private fun findTeamWithBudget(teamId: Long): Team =
        teamRepository
            .findTeamWithBudget(teamId)
            .orElseThrow { CustomLogicException(ExceptionCode.TEAM_NOT_FOUND) }

    private fun evictCache(teamId: Long) {
        cacheEvictService.evictByPrefix("recentExpenses", "team:$teamId:")
    }

    private fun adjustBudgetOnCreate(
        method: PaymentMethod,
        budget: Budget,
        amount: BigDecimal
    ) {
        if (method == PaymentMethod.CASH) budget.debitForeign(amount)
        else budget.debitKrw(amount)
    }

    private fun creditBudgetOnDelete(
        method: PaymentMethod,
        budget: Budget,
        amount: BigDecimal
    ) {
        if (method == PaymentMethod.CASH) budget.creditForeign(amount)
        else budget.creditKrw(amount)
    }

    private fun adjustBudgetOnUpdate(
        delta: BigDecimal,
        method: PaymentMethod,
        budget: Budget
    ) {
        when {
            delta > BigDecimal.ZERO -> adjustBudgetOnCreate(method, budget, delta)
            delta < BigDecimal.ZERO -> creditBudgetOnDelete(method, budget, delta.abs())
        }
    }
}
