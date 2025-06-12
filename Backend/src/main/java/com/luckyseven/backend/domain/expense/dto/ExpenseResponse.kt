package com.luckyseven.backend.domain.expense.dto

import com.luckyseven.backend.domain.expense.enums.ExpenseCategory
import com.luckyseven.backend.domain.expense.enums.PaymentMethod
import java.math.BigDecimal
import java.time.LocalDateTime


data class ExpenseResponse(
    val id: Long?,
    val description: String,
    val amount: BigDecimal,
    val category: ExpenseCategory,
    val payerId: Long?,
    val payerNickname: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val paymentMethod: PaymentMethod
)
