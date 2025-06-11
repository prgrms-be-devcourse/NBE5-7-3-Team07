package com.luckyseven.backend.domain.settlements.util;

import com.luckyseven.backend.domain.expense.entity.Expense;
import com.luckyseven.backend.domain.member.entity.Member;
import com.luckyseven.backend.domain.settlements.dto.SettlementCreateRequest;
import com.luckyseven.backend.domain.settlements.dto.SettlementResponse;
import com.luckyseven.backend.domain.settlements.entity.Settlement;
import java.math.BigDecimal;

public class SettlementMapper {

  private SettlementMapper() {
  }

  public static SettlementResponse toSettlementResponse(Settlement settlement) {
    return SettlementResponse.builder()
        .id(settlement.getId())
        .amount(settlement.getAmount())
        .createdAt(settlement.getCreatedAt())
        .updatedAt(settlement.getUpdatedAt())
        .isSettled(settlement.getIsSettled())
        .settlerId(settlement.getSettler().getId())
        .settlerNickname(settlement.getSettler().getNickname())
        .payerId(settlement.getPayer().getId())
        .payerNickname(settlement.getPayer().getNickname())
        .expenseId(settlement.getExpense().getId())
        .expenseDescription(settlement.getExpense().getDescription())
        .teamId(settlement.getExpense().getTeam().getId())
        .build();
  }

  public static Settlement fromSettlementCreateRequest(SettlementCreateRequest request,
      Member settler, Member payer, Expense expense) {
    return new Settlement(request.amount(), settler, payer, expense);
  }

  public static SettlementCreateRequest toSettlementCreateRequest(
      Expense expense,
      Long payerId,
      Long settlerId,
      BigDecimal shareAmount
  ) {
    return SettlementCreateRequest.builder()
        .expenseId(expense.getId())
        .payerId(payerId)
        .settlerId(settlerId)
        .amount(shareAmount)
        .build();
  }
}
