package com.luckyseven.backend.domain.settlements.dto

import jakarta.validation.constraints.NotNull
import java.math.BigDecimal

data class SettlementUpdateRequest(
    @field:NotNull
    val settlementId: Long,

    val amount: BigDecimal? = null,
    val settlerId: Long? = null,
    val payerId: Long? = null,
    val expenseId: Long? = null,
    val isSettled: Boolean? = null
) 