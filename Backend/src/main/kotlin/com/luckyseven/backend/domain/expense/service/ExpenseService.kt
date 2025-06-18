package com.luckyseven.backend.domain.expense.service

import com.luckyseven.backend.domain.budget.entity.Budget
import com.luckyseven.backend.domain.expense.cache.CacheEvictService
import com.luckyseven.backend.domain.expense.dto.*
import com.luckyseven.backend.domain.expense.entity.Expense
import com.luckyseven.backend.domain.expense.enums.PaymentMethod
import com.luckyseven.backend.domain.expense.mapper.ExpenseMapper
import com.luckyseven.backend.domain.expense.repository.ExpenseRepository
import com.luckyseven.backend.domain.member.entity.Member
import com.luckyseven.backend.domain.member.repository.MemberRepository
import com.luckyseven.backend.domain.settlement.app.SettlementService
import com.luckyseven.backend.domain.team.cache.TeamDashboardCacheService
import com.luckyseven.backend.domain.team.entity.Team
import com.luckyseven.backend.domain.team.repository.TeamRepository
import com.luckyseven.backend.sharedkernel.dto.PageResponse
import com.luckyseven.backend.sharedkernel.exception.CustomLogicException
import com.luckyseven.backend.sharedkernel.exception.ExceptionCode
import org.springframework.cache.annotation.CacheConfig
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

@Service
@CacheConfig(cacheNames = ["recentExpenses"])
class ExpenseService(
    private val settlementService: SettlementService,
    private val expenseRepository: ExpenseRepository,
    private val teamRepository: TeamRepository,
    private val memberRepository: MemberRepository,
    private val cacheEvictService: CacheEvictService,
    private val teamDashboardCacheService: TeamDashboardCacheService
) {

    companion object {
        private const val CACHE_KEY_TEMPLATE = "team:%d:page:%d:size:%d"
        private const val CACHE_PREFIX_TEMPLATE = "team:%d:"
    }

    @Transactional
    fun saveExpense(teamId: Long, request: ExpenseRequest): CreateExpenseResponse {
        val team = findTeamWithBudget(teamId)
        val payer = findMemberOrThrow(request.payerId)
        val budget = findBudgetOrThrow(team)

        adjustBudget(request.paymentMethod, budget, request.amount)

        val expense = createAndSaveExpense(request, team, payer)

        val isForeign = request.paymentMethod == PaymentMethod.CASH
        val amountInKrw = calculateAmountInKrw(request, budget)
        val settlementRequest = toSettlementRequest(request, amountInKrw, isForeign)

        createSettlements(settlementRequest, payer, expense)

        evictCache(teamId)
        return ExpenseMapper.toCreateExpenseResponse(expense, budget)
    }

    @Transactional(readOnly = true)
    fun getExpense(expenseId: Long): ExpenseResponse {
        val expense = findExpenseWithPayer(expenseId)
        return ExpenseMapper.toExpenseResponse(expense)
    }

    @Transactional(readOnly = true)
    @Cacheable(key = "T(String).format('${CACHE_KEY_TEMPLATE}', #teamId, #pageable.pageNumber, #pageable.pageSize)")
    fun getExpenses(teamId: Long, pageable: Pageable): PageResponse<ExpenseResponse> {
        validateTeamExists(teamId)
        val page = expenseRepository.findResponsesByTeamId(teamId, pageable)
        return ExpenseMapper.toPageResponse(page)
    }

    @Transactional
    fun updateExpense(expenseId: Long, request: ExpenseUpdateRequest): CreateExpenseResponse {
        val expense = findExpenseWithBudgetOrThrow(expenseId)
        val budget = findBudgetOrThrow(expense.team)

        val amountDelta = calculateAmountDelta(expense.amount, request.amount)
        adjustBudget(expense.paymentMethod, budget, amountDelta)

        updateExpenseDetails(expense, request)

        evictCache(expense.team.id!!)
        return ExpenseMapper.toCreateExpenseResponse(expense, budget)
    }

    @Transactional
    fun deleteExpense(expenseId: Long): ExpenseBalanceResponse {
        val expense = findExpenseWithBudgetOrThrow(expenseId)
        val budget = findBudgetOrThrow(expense.team)

        adjustBudget(expense.paymentMethod, budget, expense.amount.negate())

        expenseRepository.delete(expense)
        evictCache(expense.team.id!!)

        return ExpenseMapper.toExpenseBalanceResponse(budget)
    }

    private fun createAndSaveExpense(request: ExpenseRequest, team: Team, payer: Member): Expense {
        val expense = ExpenseMapper.fromExpenseRequest(request, team, payer)
        return expenseRepository.save(expense)
    }

    private fun createSettlements(request: ExpenseRequest, payer: Member, expense: Expense) {
        settlementService.createAllSettlements(request, payer, expense)
    }

    private fun calculateAmountDelta(
        originalAmount: BigDecimal,
        newAmount: BigDecimal?
    ): BigDecimal {
        val actualNewAmount = newAmount ?: originalAmount
        return actualNewAmount.subtract(originalAmount)
    }

    private fun updateExpenseDetails(expense: Expense, request: ExpenseUpdateRequest) {
        val newAmount = request.amount ?: expense.amount
        expense.update(request.description, newAmount, request.category)
    }

    private fun validateTeamExists(teamId: Long) {
        if (!teamRepository.existsById(teamId)) {
            throw CustomLogicException(ExceptionCode.TEAM_NOT_FOUND)
        }
    }

    private fun calculateAmountInKrw(request: ExpenseRequest, budget: Budget): BigDecimal {
        return if (request.paymentMethod == PaymentMethod.CASH) {
            request.amount.multiply(budget.avgExchangeRate)
        } else {
            request.amount
        }
    }

    private fun toSettlementRequest(
        request: ExpenseRequest,
        amountInKrw: BigDecimal,
        isForeign: Boolean
    ): ExpenseRequest {
        return if (isForeign) {
            request.copy(amount = amountInKrw)
        } else {
            request
        }
    }


    private fun adjustBudget(method: PaymentMethod, budget: Budget, delta: BigDecimal) {
        when {
            delta > BigDecimal.ZERO -> {
                if (method == PaymentMethod.CASH) {
                    budget.debitForeign(delta)
                } else {
                    budget.debitKrw(delta)
                }
            }

            delta < BigDecimal.ZERO -> {
                val absoluteAmount = delta.abs()
                if (method == PaymentMethod.CASH) {
                    budget.creditForeign(absoluteAmount)
                } else {
                    budget.creditKrw(absoluteAmount)
                }
            }
        }
    }

    private fun evictCache(teamId: Long) {
        val cachePrefix = String.format(CACHE_PREFIX_TEMPLATE, teamId)
        cacheEvictService.evictByPrefix("recentExpenses", cachePrefix)
        teamDashboardCacheService.evictTeamDashboardCache(teamId)
    }

    private fun findExpenseWithPayer(expenseId: Long): Expense =
        expenseRepository.findByIdWithPayer(expenseId)
            ?: throw CustomLogicException(ExceptionCode.EXPENSE_NOT_FOUND)

    private fun findBudgetOrThrow(team: Team): Budget =
        team.budget ?: throw CustomLogicException(ExceptionCode.BUDGET_NOT_FOUND)

    private fun findExpenseWithBudgetOrThrow(expenseId: Long): Expense =
        expenseRepository.findWithTeamAndBudgetById(expenseId)
            ?: throw CustomLogicException(ExceptionCode.EXPENSE_NOT_FOUND)

    private fun findTeamWithBudget(teamId: Long): Team =
        teamRepository.findTeamWithBudget(teamId)
            ?: throw CustomLogicException(ExceptionCode.TEAM_NOT_FOUND)

    private fun findMemberOrThrow(id: Long): Member =
        memberRepository.findByIdOrNull(id)
            ?: throw CustomLogicException(ExceptionCode.MEMBER_ID_NOTFOUND, id)
}
