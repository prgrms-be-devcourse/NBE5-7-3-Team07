package com.luckyseven.backend.domain.expense.dto

import java.math.BigDecimal
import java.time.LocalDateTime

data class CreateExpenseResponse(
    val id: Long?,
    val amount: BigDecimal,
    val foreignBalance: BigDecimal?,
    val balance: BigDecimal?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)
