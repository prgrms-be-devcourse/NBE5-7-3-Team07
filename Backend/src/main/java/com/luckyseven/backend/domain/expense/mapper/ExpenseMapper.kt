package com.luckyseven.backend.domain.expense.mapper;

import com.luckyseven.backend.domain.budget.entity.Budget;
import com.luckyseven.backend.domain.expense.dto.CreateExpenseResponse;
import com.luckyseven.backend.domain.expense.dto.ExpenseBalanceResponse;
import com.luckyseven.backend.domain.expense.dto.ExpenseRequest;
import com.luckyseven.backend.domain.expense.dto.ExpenseResponse;
import com.luckyseven.backend.domain.expense.entity.Expense;
import com.luckyseven.backend.domain.member.entity.Member;
import com.luckyseven.backend.domain.team.entity.Team;
import com.luckyseven.backend.sharedkernel.dto.PageResponse;
import org.springframework.data.domain.Page;

public class ExpenseMapper {

  private ExpenseMapper() {
  }

  public static Expense fromExpenseRequest(ExpenseRequest request, Team team,
      Member payer) {
    return Expense.builder()
        .description(request.description)
        .amount(request.amount)
        .paymentMethod(request.paymentMethod)
        .category(request.category)
        .payer(payer)
        .team(team)
        .build();
  }

  public static CreateExpenseResponse toCreateExpenseResponse(Expense expense, Budget budget) {
    return CreateExpenseResponse.builder()
        .amount(expense.getAmount())
        .id(expense.getId())
        .foreignBalance(budget.getForeignBalance())
        .balance(budget.getBalance())
        .createdAt(expense.getCreatedAt())
        .updatedAt(expense.getUpdatedAt())
        .build();
  }

  public static ExpenseBalanceResponse toExpenseBalanceResponse(Budget budget) {
    return ExpenseBalanceResponse
        .builder()
        .balance(budget.getBalance())
        .foreignBalance(budget.getForeignBalance())
        .build();
  }

  public static ExpenseResponse toExpenseResponse(Expense expense) {
    return ExpenseResponse.builder()
        .id(expense.getId())
        .description(expense.getDescription())
        .amount(expense.getAmount())
        .category(expense.getCategory())
        .payerId(expense.getPayer().getId())
        .payerNickname(expense.getPayer().getNickname())
        .createdAt(expense.getCreatedAt())
        .updatedAt(expense.getUpdatedAt())
        .paymentMethod(expense.getPaymentMethod())
        .build();
  }

  public static PageResponse<ExpenseResponse> toPageResponse(Page<ExpenseResponse> page) {
    return PageResponse.fromPage(page);
  }
}
