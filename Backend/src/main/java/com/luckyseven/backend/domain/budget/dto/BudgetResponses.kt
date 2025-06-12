package com.luckyseven.backend.domain.budget.dto

import com.luckyseven.backend.domain.budget.entity.CurrencyCode
import java.math.BigDecimal
import java.time.LocalDateTime

data class BudgetCreateResponse(

    val id: Long,
    val createdAt: LocalDateTime,

    val setBy: Long,
    val balance: BigDecimal,

    val avgExchangeRate: BigDecimal?,
    val foreignBalance: BigDecimal?

)

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

data class BudgetUpdateResponse(
    val id: Long,
    val updatedAt: LocalDateTime,

    val setBy: Long,
    val balance: BigDecimal,
    val foreignCurrency: CurrencyCode,

    val avgExchangeRate: BigDecimal?,
    val foreignBalance: BigDecimal?
)