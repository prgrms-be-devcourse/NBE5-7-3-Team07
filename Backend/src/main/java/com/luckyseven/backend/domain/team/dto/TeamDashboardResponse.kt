package com.luckyseven.backend.domain.team.dto

import com.fasterxml.jackson.annotation.JsonFormat
import com.luckyseven.backend.domain.budget.entity.CurrencyCode
import com.luckyseven.backend.domain.expense.enums.ExpenseCategory
import com.luckyseven.backend.domain.expense.enums.PaymentMethod
import java.math.BigDecimal
import java.time.LocalDateTime


data class TeamDashboardResponse(
    var team_id: Long? = null,

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
}