package com.luckyseven.backend.domain.expense.dto;

import com.luckyseven.backend.domain.expense.enums.ExpenseCategory;
import com.luckyseven.backend.domain.expense.enums.PaymentMethod;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record ExpenseResponse(
    Long id,
    String description,
    BigDecimal amount,
    ExpenseCategory category,
    Long payerId,
    String payerNickname,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    PaymentMethod paymentMethod
) {

}
