package com.luckyseven.backend.domain.expense.util

import com.luckyseven.backend.domain.expense.dto.CreateExpenseResponse
import com.luckyseven.backend.domain.expense.dto.ExpenseBalanceResponse
import com.luckyseven.backend.domain.expense.dto.ExpenseRequest
import com.luckyseven.backend.domain.expense.dto.ExpenseResponse
import com.luckyseven.backend.domain.expense.enums.ExpenseCategory
import com.luckyseven.backend.domain.expense.enums.PaymentMethod
import java.math.BigDecimal
import java.time.LocalDateTime

object ExpenseTestUtils {
    fun buildRequest(
        description: String = "Test expense",
        amount: BigDecimal = BigDecimal("1000.00"),
        category: ExpenseCategory = ExpenseCategory.MEAL,
        payerId: Long = 1L,
        paymentMethod: PaymentMethod = PaymentMethod.CASH,
        settlerIds: List<Long> = listOf(1L)
    ) = ExpenseRequest(
        description = description,
        amount = amount,
        category = category,
        payerId = payerId,
        paymentMethod = paymentMethod,
        settlerId = settlerIds.toMutableList()
    )

    fun buildCreateResponse(
        id: Long = 1L,
        amount: BigDecimal = BigDecimal("1000.00"),
        foreignBalance: BigDecimal = BigDecimal("8000.00"),
        balance: BigDecimal = BigDecimal("9000.00"),
        createdAt: LocalDateTime = LocalDateTime.now(),
        updatedAt: LocalDateTime = LocalDateTime.now()
    ) = CreateExpenseResponse(
        id = id,
        amount = amount,
        foreignBalance = foreignBalance,
        balance = balance,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    fun buildExpenseResponse(
        id: Long = 1L,
        description: String = "Test expense",
        amount: BigDecimal = BigDecimal("1000.00"),
        category: ExpenseCategory = ExpenseCategory.MEAL,
        paymentMethod: PaymentMethod = PaymentMethod.CASH,
        payerId: Long = 1L,
        payerNickname: String = "TestUser",
        createdAt: LocalDateTime = LocalDateTime.now(),
        updatedAt: LocalDateTime = LocalDateTime.now()
    ) = ExpenseResponse(
        id = id,
        description = description,
        amount = amount,
        category = category,
        paymentMethod = paymentMethod,
        payerId = payerId,
        payerNickname = payerNickname,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    fun buildBalanceResponse(
        foreignBalance: BigDecimal = BigDecimal("8000.00"),
        balance: BigDecimal = BigDecimal("9000.00")
    ) = ExpenseBalanceResponse(
        foreignBalance = foreignBalance,
        balance = balance
    )
}
