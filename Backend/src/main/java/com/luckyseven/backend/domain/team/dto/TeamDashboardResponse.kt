package com.luckyseven.backend.domain.team.dto

import com.fasterxml.jackson.annotation.JsonFormat
import com.luckyseven.backend.domain.budget.entity.CurrencyCode
import com.luckyseven.backend.domain.expense.enums.ExpenseCategory
import com.luckyseven.backend.domain.expense.enums.PaymentMethod
import java.math.BigDecimal
import java.time.LocalDateTime


data class TeamDashboardResponse(
    val teamId: Long,

    val teamName: String,

    val teamCode: String,

    val teamPassword: String,

    val foreignCurrency: CurrencyCode? = null,

    val balance: BigDecimal? = null,

    val foreignBalance: BigDecimal? = null,

    val totalAmount: BigDecimal? = null,

    val avgExchangeRate: BigDecimal? = null,

    val updatedAt: LocalDateTime,

    val expenseList: List<ExpenseDto?> = emptyList(),

    val categoryExpenseSumList: List<CategoryExpenseSumDto?> = emptyList()
) {


    data class CategoryExpenseSumDto(
        val category: ExpenseCategory? = null,
        val totalAmount: BigDecimal? = null
    )

    data class ExpenseDto(
        val id: Long,
        val description: String? = null,
        val amount: BigDecimal,
        val category: ExpenseCategory,
        val paymentMethod: PaymentMethod,

        @field:JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        val date: LocalDateTime,
        val payerNickname: String
    )
}