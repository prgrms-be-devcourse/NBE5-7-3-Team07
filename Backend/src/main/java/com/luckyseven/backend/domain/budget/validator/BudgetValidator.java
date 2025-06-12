package com.luckyseven.backend.domain.budget.validator;

import com.luckyseven.backend.domain.budget.dao.BudgetRepository;
import com.luckyseven.backend.domain.budget.dto.BudgetAddRequest;
import com.luckyseven.backend.domain.budget.dto.BudgetCreateRequest;
import com.luckyseven.backend.domain.budget.dto.BudgetUpdateRequest;
import com.luckyseven.backend.domain.budget.entity.Budget;
import com.luckyseven.backend.sharedkernel.exception.CustomLogicException;
import com.luckyseven.backend.sharedkernel.exception.ExceptionCode;
import java.math.BigDecimal;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BudgetValidator {

  private final BudgetRepository budgetRepository;

  public void validateBudgetNotExist(Long teamId) {
    Optional<Budget> budgetOptional = budgetRepository.findByTeamId(teamId);

    if (budgetOptional.isPresent()) {
      throw new CustomLogicException(ExceptionCode.BUDGET_CONFLICT,
          "budgetId: " + budgetOptional.get().getId());
    }
  }

  public Budget validateBudgetExist(Long teamId) {
    return budgetRepository.findByTeamId(teamId)
        .orElseThrow(() ->
            new CustomLogicException(ExceptionCode.TEAM_NOT_FOUND, "teamId: " + teamId)
        );
  }

  public void validateRequest(BudgetCreateRequest request) {
    validateIsExchangedRequest(request.isExchanged(), request.exchangeRate());
  }

  public void validateRequest(BudgetUpdateRequest request) {
    validateIsExchangedRequest(request.isExchanged(), request.exchangeRate());
  }

  public void validateRequest(BudgetAddRequest request) {
    validateIsExchangedRequest(request.isExchanged(), request.exchangeRate());
    // additionalAmount 입력 시 isExchanged 필수
  }

  private void validateIsExchangedRequest(boolean isExchanged, BigDecimal exchangeRate) {
    if (isExchanged && exchangeRate == null) {
      throw new CustomLogicException(ExceptionCode.BAD_REQUEST, "환전 여부가 true인데 환율이 없습니다.");
    }
    if (!isExchanged && exchangeRate != null) {
      throw new CustomLogicException(ExceptionCode.BAD_REQUEST, "환전 여부가 false인데 환율이 있습니다.");
    }
  }
}