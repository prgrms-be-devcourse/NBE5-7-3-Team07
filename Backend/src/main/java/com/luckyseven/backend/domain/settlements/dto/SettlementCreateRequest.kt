package com.luckyseven.backend.domain.settlements.dto

import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.Digits
import jakarta.validation.constraints.NotNull
import java.math.BigDecimal

data class SettlementCreateRequest(
    @field:NotNull
    @field:DecimalMin(value = "0.0", inclusive = false)
    @field:Digits(integer = 15, fraction = 2)
    val amount: BigDecimal,

    @field:NotNull
    val settlerId: Long,

    @field:NotNull
    val payerId: Long,

    @field:NotNull
    val expenseId: Long
)