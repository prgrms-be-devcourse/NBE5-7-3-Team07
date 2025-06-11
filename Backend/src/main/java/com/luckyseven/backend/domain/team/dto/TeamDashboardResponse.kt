package com.luckyseven.backend.domain.team.dto

import com.fasterxml.jackson.annotation.JsonFormat
import com.luckyseven.backend.domain.budget.entity.Budget
import com.luckyseven.backend.domain.budget.entity.CurrencyCode
import com.luckyseven.backend.domain.expense.entity.Expense
import com.luckyseven.backend.domain.expense.enums.ExpenseCategory
import com.luckyseven.backend.domain.expense.enums.PaymentMethod
import com.luckyseven.backend.domain.expense.repository.CategoryExpenseSum
import com.luckyseven.backend.domain.team.entity.Team
import java.math.BigDecimal
import java.time.LocalDateTime
import kotlin.math.exp


data class TeamDashboardResponse(
    var teamId: Long? = null,

    var teamName: String? = null,

    var teamCode: String? = null,

    var teamPassword: String? = null,

    var foreignCurrency: CurrencyCode? = null,

    var balance: BigDecimal? = null,

    var foreignBalance: BigDecimal? = null,
    var totalAmount: BigDecimal? = null,

    var avgExchangeRate: BigDecimal? = null,

    var updatedAt: LocalDateTime? = null,

    val expenseList: List<ExpenseDto?> = emptyList(),

    val categoryExpenseSumList: List<CategoryExpenseSumDto?> = emptyList()
) {


    data class CategoryExpenseSumDto(
        var category: ExpenseCategory? = null,
        var totalAmount: BigDecimal? = null
    )

    data class ExpenseDto(
        var id: Long? = null,
        var description: String? = null,
        var amount: BigDecimal? = null,
        var category: ExpenseCategory? = null,
        var paymentMethod: PaymentMethod? = null,

        @field:JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        var date: LocalDateTime? = null,
        var payerNickname: String? = null
    )

    companion object{
        fun toTeamDashboardResponse(
            team: Team?,
            budget: Budget?,
            expenses: List<Expense>?,
            categoryExpenseSums: List<CategoryExpenseSum>?
        ): TeamDashboardResponse?{
            if(team == null) return null

            val expenseDtoList = expenses?.map { expense ->
                ExpenseDto(
                    id = expense.id,
                    description = expense.description,
                    amount = expense.amount,
                    paymentMethod = expense.paymentMethod,
                    category = expense.category,
                    date = expense.createdAt,
                    payerNickname = expense.payer?.nickname
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
                balance = budget?.balance ?: BigDecimal.ZERO,
                foreignBalance = budget?.foreignBalance ?: BigDecimal.ZERO,
                totalAmount = budget?.totalAmount ?: BigDecimal.ZERO,
                avgExchangeRate = budget?.avgExchangeRate ?: BigDecimal.ZERO,
                updatedAt = budget?.updatedAt,
                expenseList = expenseDtoList,
                categoryExpenseSumList = categorySumDtos
            )
        }
    }
}