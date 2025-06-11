package com.luckyseven.backend.domain.settlements.dto;

import com.luckyseven.backend.domain.settlements.entity.Settlement;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import java.math.BigDecimal;
import lombok.Builder;

/**
 * DTO for {@link Settlement}
 */
@Builder
public record SettlementUpdateRequest(
    @DecimalMin(value = "0.0", inclusive = false)
    @Digits(integer = 15, fraction = 2)
    BigDecimal amount,

    Boolean isSettled,

    Long settlerId,

    Long payerId,

    Long expenseId) {

}