package com.luckyseven.backend.domain.expense.dto

import com.luckyseven.backend.domain.expense.enums.ExpenseCategory
import jakarta.validation.constraints.DecimalMin
import java.math.BigDecimal

data class ExpenseUpdateRequest(

    val description: String? = null,

    @field:DecimalMin(value = "0.00", message = "금액은 0 이상이어야 합니다.")
    val amount: BigDecimal? = null,

    val category: ExpenseCategory? = null
)
