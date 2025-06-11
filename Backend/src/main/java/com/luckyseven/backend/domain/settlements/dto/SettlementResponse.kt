package com.luckyseven.backend.domain.settlements.dto

import java.math.BigDecimal
import java.time.LocalDateTime

data class SettlementResponse(
    val id: Long,
    val amount: BigDecimal,
    val isSettled: Boolean,
    val settlerId: Long,
    val settlerName: String,
    val payerId: Long,
    val payerName: String,
    val expenseId: Long,
    val expenseName: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)