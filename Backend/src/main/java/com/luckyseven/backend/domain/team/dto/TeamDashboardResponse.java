package com.luckyseven.backend.domain.team.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.luckyseven.backend.domain.budget.entity.CurrencyCode;
import com.luckyseven.backend.domain.expense.enums.ExpenseCategory;
import com.luckyseven.backend.domain.expense.enums.PaymentMethod;
import com.luckyseven.backend.domain.expense.repository.CategoryExpenseSum;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamDashboardResponse {

  private Long team_id;

  private String teamName;

  private String teamCode;

  private String teamPassword;

  private CurrencyCode foreignCurrency;

  private BigDecimal balance;

  private BigDecimal foreignBalance;

  private BigDecimal totalAmount;

  private BigDecimal avgExchangeRate;

  private LocalDateTime updatedAt;

  private List<ExpenseDto> expenseList = new ArrayList<>();

  private List<CategoryExpenseSumDto> categoryExpenseSumList = new ArrayList<>();


  @Getter
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class CategoryExpenseSumDto {
    private ExpenseCategory category;
    private BigDecimal totalAmount;
  }

  @Getter
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class ExpenseDto {

    private Long id;
    private String description;
    private BigDecimal amount;
    private ExpenseCategory category;
    private PaymentMethod paymentMethod;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime date;
    private String payerNickname;
  }
}