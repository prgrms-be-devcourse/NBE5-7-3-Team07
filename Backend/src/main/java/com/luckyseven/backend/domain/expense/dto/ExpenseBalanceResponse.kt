package com.luckyseven.backend.domain.expense.dto;

import java.math.BigDecimal;
import lombok.Builder;

@Builder
public record ExpenseBalanceResponse(
    BigDecimal foreignBalance,
    BigDecimal balance
) {

}
