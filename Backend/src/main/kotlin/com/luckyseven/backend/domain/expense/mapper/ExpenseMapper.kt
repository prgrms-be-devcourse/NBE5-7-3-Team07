package com.luckyseven.backend.domain.expense.mapper

import com.luckyseven.backend.domain.budget.entity.Budget
import com.luckyseven.backend.domain.expense.dto.CreateExpenseResponse
import com.luckyseven.backend.domain.expense.dto.ExpenseBalanceResponse
import com.luckyseven.backend.domain.expense.dto.ExpenseRequest
import com.luckyseven.backend.domain.expense.dto.ExpenseResponse
import com.luckyseven.backend.domain.expense.entity.Expense
import com.luckyseven.backend.domain.member.entity.Member
import com.luckyseven.backend.domain.team.entity.Team
import com.luckyseven.backend.sharedkernel.dto.PageResponse
import org.springframework.data.domain.Page
import java.time.LocalDateTime

object ExpenseMapper {

    fun fromExpenseRequest(request: ExpenseRequest, team: Team, payer: Member): Expense =
        Expense(
            description = request.description,
            amount = request.amount,
            paymentMethod = request.paymentMethod,
            category = request.category,
            payer = payer,
            team = team
        )

    fun toCreateExpenseResponse(expense: Expense, budget: Budget): CreateExpenseResponse =
        CreateExpenseResponse(
            id = expense.id,
            amount = expense.amount,
            balance = budget.balance,
            foreignBalance = budget.foreignBalance,
            createdAt = expense.createdAt ?: LocalDateTime.now(),
            updatedAt = expense.updatedAt ?: LocalDateTime.now(),
        )

    fun toExpenseBalanceResponse(budget: Budget): ExpenseBalanceResponse =
        ExpenseBalanceResponse(
            balance = budget.balance,
            foreignBalance = budget.foreignBalance
        )

    fun toExpenseResponse(expense: Expense): ExpenseResponse =
        ExpenseResponse(
            id = expense.id,
            description = expense.description,
            amount = expense.amount,
            category = expense.category,
            payerId = expense.payer.id,
            payerNickname = expense.payer.nickname,
            createdAt = expense.createdAt ?: LocalDateTime.now(),
            updatedAt = expense.updatedAt ?: LocalDateTime.now(),
            paymentMethod = expense.paymentMethod
        )

    fun toPageResponse(page: Page<ExpenseResponse>): PageResponse<ExpenseResponse> =
        PageResponse.fromPage(page)
}
