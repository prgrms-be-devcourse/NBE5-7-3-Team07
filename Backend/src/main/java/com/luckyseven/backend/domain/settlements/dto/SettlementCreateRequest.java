package com.luckyseven.backend.domain.settlements.dto;

import com.luckyseven.backend.domain.settlements.entity.Settlement;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Builder;

/**
 * DTO for {@link Settlement}
 */
@Builder
public record SettlementCreateRequest(

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    @Digits(integer = 15, fraction = 2)
    BigDecimal amount,

    @NotNull
    Long settlerId,

    @NotNull
    Long payerId,

    @NotNull
    Long expenseId

) {

}