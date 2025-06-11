package com.luckyseven.backend.domain.settlements.dto

data class SettlementSearchCondition(
    val expenseId: Long?,
    val settlerId: Long?,
    val payerId: Long?,
    val isSettled: Boolean?
)
