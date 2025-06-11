package com.luckyseven.backend.domain.budget.mapper;

import com.luckyseven.backend.domain.budget.dto.BudgetCreateRequest;
import com.luckyseven.backend.domain.budget.dto.BudgetCreateResponse;
import com.luckyseven.backend.domain.budget.dto.BudgetReadResponse;
import com.luckyseven.backend.domain.budget.dto.BudgetUpdateRequest;
import com.luckyseven.backend.domain.budget.dto.BudgetUpdateResponse;
import com.luckyseven.backend.domain.budget.entity.Budget;
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.stereotype.Component;

@Component
public class BudgetMapper {

  public BudgetCreateResponse toCreateResponse(Budget budget) {
    return new BudgetCreateResponse(
        budget.getId(),
        budget.getCreatedAt(),
        budget.getSetBy(),
        budget.getBalance(),
        budget.getAvgExchangeRate(),
        budget.getForeignBalance()
    );
  }

  public BudgetReadResponse toReadResponse(Budget budget) {
    return new BudgetReadResponse(
        budget.getId(),
        budget.getUpdatedAt(),
        budget.getSetBy(),
        budget.getTotalAmount(),
        budget.getBalance(),
        budget.getForeignCurrency(),
        budget.getAvgExchangeRate(),
        budget.getForeignBalance()
    );
  }

  public BudgetUpdateResponse toUpdateResponse(Budget budget) {
    return new BudgetUpdateResponse(
        budget.getId(),
        budget.getUpdatedAt(),
        budget.getSetBy(),
        budget.getBalance(),
        budget.getForeignCurrency(),
        budget.getAvgExchangeRate(),
        budget.getForeignBalance()
    );
  }

}
