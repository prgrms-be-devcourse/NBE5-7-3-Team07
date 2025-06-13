package com.luckyseven.backend.domain.expense.dto

import java.math.BigDecimal


data class ExpenseBalanceResponse(
    val foreignBalance: BigDecimal?,
    val balance: BigDecimal?
)
