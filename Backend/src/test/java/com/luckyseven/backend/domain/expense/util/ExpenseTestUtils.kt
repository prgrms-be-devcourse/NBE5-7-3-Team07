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
    fun buildRequest() = ExpenseRequest(
        description = "Test expense",
        amount = BigDecimal("1000.00"),
        category = ExpenseCategory.MEAL,
        payerId = 1L,
        paymentMethod = PaymentMethod.CASH,
        settlerId = mutableListOf(1L)
    )

    fun buildCreateResponse() = CreateExpenseResponse(
        id = 1L,
        amount = BigDecimal("1000.00"),
        foreignBalance = BigDecimal("8000.00"),
        balance = BigDecimal("9000.00"),
        createdAt = LocalDateTime.now(),
        updatedAt = LocalDateTime.now()
    )

    fun buildExpenseResponse() = ExpenseResponse(
        id = 1L,
        description = "Test expense",
        amount = BigDecimal("1000.00"),
        category = ExpenseCategory.MEAL,
        paymentMethod = PaymentMethod.CASH,
        payerId = 1L,
        payerNickname = "TestUser",
        createdAt = LocalDateTime.now(),
        updatedAt = LocalDateTime.now()
    )

    fun buildBalanceResponse() = ExpenseBalanceResponse(
        foreignBalance = BigDecimal("8000.00"),
        balance = BigDecimal("9000.00")
    )
}
