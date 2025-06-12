package com.luckyseven.backend.domain.budget.dto

import com.luckyseven.backend.domain.budget.entity.CurrencyCode
import jakarta.validation.constraints.NotNull
import java.math.BigDecimal
import java.time.LocalDateTime

data class BudgetReadResponse(

    val id: Long,
    val updatedAt: LocalDateTime,

    val setBy: Long,
    val totalAmount: BigDecimal,

    val balance: BigDecimal,
    val foreignCurrency: CurrencyCode,

    val avgExchangeRate: BigDecimal?,
    val foreignBalance: BigDecimal?
)
