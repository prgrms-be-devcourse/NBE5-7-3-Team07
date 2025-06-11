package com.luckyseven.backend.domain.expense.dto;

import com.luckyseven.backend.domain.expense.enums.ExpenseCategory;
import jakarta.validation.constraints.DecimalMin;
import java.math.BigDecimal;
import lombok.Builder;

@Builder
public record ExpenseUpdateRequest(
    String description,

    @DecimalMin(value = "0.00", message = "금액은 0 이상이어야 합니다.")
    BigDecimal amount,

    ExpenseCategory category
) {

}

