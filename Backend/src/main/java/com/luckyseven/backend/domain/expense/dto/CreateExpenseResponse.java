package com.luckyseven.backend.domain.expense.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record CreateExpenseResponse(
    Long id,
    BigDecimal amount,
    BigDecimal foreignBalance,
    BigDecimal balance,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {

}
