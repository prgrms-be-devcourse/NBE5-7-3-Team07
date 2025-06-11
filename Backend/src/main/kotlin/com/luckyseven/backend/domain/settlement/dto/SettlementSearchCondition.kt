package com.luckyseven.backend.domain.settlement.dto

data class SettlementSearchCondition(
    val expenseId: Long?,
    val settlerId: Long?,
    val payerId: Long?,
    val isSettled: Boolean?
)
