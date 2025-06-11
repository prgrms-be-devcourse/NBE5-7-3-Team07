package com.luckyseven.backend.domain.settlements.dto

import java.math.BigDecimal
import java.time.LocalDateTime

data class SettlementResponse(
    val id: Long?,
    val amount: BigDecimal,
    val isSettled: Boolean,
    val settlerId: Long,
    val settlerNickName: String,
    val payerId: Long,
    val payerNickName: String,
    val expenseId: Long,
    val expenseDescription: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val teamId: Long
)