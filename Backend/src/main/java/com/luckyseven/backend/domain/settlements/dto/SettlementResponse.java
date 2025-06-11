package com.luckyseven.backend.domain.settlements.dto;

import com.luckyseven.backend.domain.settlements.entity.Settlement;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Builder;

/**
 * DTO for {@link Settlement}
 */
@Builder
public record SettlementResponse(
    Long id,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    BigDecimal amount,
    Boolean isSettled,
    Long settlerId,
    String settlerNickname,
    Long payerId,
    String payerNickname,
    Long expenseId,
    String expenseDescription,
    Long teamId
) {

}