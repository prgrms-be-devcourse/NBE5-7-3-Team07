package com.luckyseven.backend.domain.settlements.dto;

import lombok.Builder;

@Builder
public record SettlementSearchCondition(

    Long expenseId,
    Long settlerId,
    Long payerId,
    Boolean isSettled
) {

}
