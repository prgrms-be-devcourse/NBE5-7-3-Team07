package com.luckyseven.backend.domain.expense.repository

import com.luckyseven.backend.domain.expense.enums.ExpenseCategory
import java.math.BigDecimal

interface CategoryExpenseSum {
    val category: ExpenseCategory
    val totalAmount: BigDecimal
}
