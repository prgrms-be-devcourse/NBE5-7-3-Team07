package com.luckyseven.backend.domain.budget.dto

import jakarta.validation.constraints.NotNull
import java.math.BigDecimal
import java.time.LocalDateTime

data class BudgetCreateResponse(

    val id: Long,
    val createdAt: LocalDateTime,

    val setBy: Long,
    val balance: BigDecimal,

    val avgExchangeRate: BigDecimal,
    val foreignBalance: BigDecimal

)
