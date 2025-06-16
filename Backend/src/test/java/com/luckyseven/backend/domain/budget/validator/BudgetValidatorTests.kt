package com.luckyseven.backend.domain.budget.validator;

import static com.luckyseven.backend.domain.budget.util.TestUtils.genBudget;
import static com.luckyseven.backend.domain.budget.util.TestUtils.genBudgetAddReq;
import static com.luckyseven.backend.domain.budget.util.TestUtils.genBudgetCreateReq;
import static com.luckyseven.backend.domain.budget.util.TestUtils.genBudgetUpdateReq;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.luckyseven.backend.domain.budget.dao.BudgetRepository;
import com.luckyseven.backend.domain.budget.dto.BudgetAddRequest;
import com.luckyseven.backend.domain.budget.dto.BudgetCreateRequest;
import com.luckyseven.backend.domain.budget.dto.BudgetUpdateRequest;
import com.luckyseven.backend.domain.budget.entity.Budget;
import com.luckyseven.backend.domain.budget.entity.CurrencyCode;
import com.luckyseven.backend.sharedkernel.exception.CustomLogicException;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BudgetValidatorTests {

  @InjectMocks
  private BudgetValidator budgetValidator;

  @Mock
  private BudgetRepository budgetRepository;

  @Test
  @DisplayName("validateBudgetNotExist는 teamId에 해당하는 예산이 존재하지 않다는 것을 확인한다")
  void validateBudgetNotExist_verify_budget_not_exist() throws Exception {

    Long teamId = 1L;
    Optional<Budget> budgetOptional = Optional.empty();

    when(budgetRepository.findByTeamId(teamId)).thenReturn(budgetOptional);

    budgetValidator.validateBudgetNotExist(teamId);

    assertThat(budgetOptional.isPresent()).isFalse();

  }

  @Test
  @DisplayName("validateBudgetNotExist는 teamId에 해당하는 예산이 존재하면 409 CONFLICT가 발생한다")
  void validateBudgetNotExist_should_return_409() throws Exception {

    Long teamId = 1L;
    Budget budget = genBudget();

    when(budgetRepository.findByTeamId(teamId)).thenReturn(Optional.of(budget));

    assertThatThrownBy(() -> budgetValidator.validateBudgetNotExist(teamId))
        .isInstanceOf(CustomLogicException.class)
        .hasMessageContaining("팀에 이미 존재하는 예산이 있습니다.");

  }

  @Test
  @DisplayName("validateBudgetExist는 teamId에 해당하는 예산이 존재하면 Budget을 반환한다")
  void validateBudgetExist_should_return_Budget() throws Exception {

    Long teamId = 1L;
    Budget expectedBudget = genBudget();

    when(budgetRepository.findByTeamId(teamId)).thenReturn(Optional.of(expectedBudget));

    Budget actualBudget = budgetValidator.validateBudgetExist(teamId);

    assertThat(actualBudget.getTotalAmount()).isEqualTo(expectedBudget.getTotalAmount());
    assertThat(actualBudget.getBalance()).isEqualTo(expectedBudget.getBalance());
    assertThat(actualBudget.getForeignCurrency()).isEqualTo(expectedBudget.getForeignCurrency());

  }

  @Test
  @DisplayName("validateBudgetExist는 teamId에 해당하는 예산이 존재하지 않으면 404 NOT FOUND가 발생한다")
  void validateBudgetExist_should_return_404() throws Exception {

    Long teamId = 1L;

    assertThatThrownBy(() -> budgetValidator.validateBudgetExist(teamId))
        .isInstanceOf(CustomLogicException.class)
        .hasMessageContaining("팀을 찾을 수 없습니다.");

  }

  @Test
  @DisplayName("validateRequest는 유효한 BudgetCreateRequest인지 검증한다")
  void validateRequest_verify_BudgetCreateRequest() throws Exception {

    BudgetCreateRequest req = genBudgetCreateReq();

    assertThatCode(() -> budgetValidator.validateRequest(req))
        .doesNotThrowAnyException();

  }

  @Test
  @DisplayName("validateRequest는 환전 여부가 true인데 환율이 없으면 400 BAD REQUEST가 발생한다")
  void validateRequest_with_CreateReq_should_return_400_1() throws Exception {

    BudgetCreateRequest req = new BudgetCreateRequest(
        BigDecimal.valueOf(100000),
        true,
        null,
        CurrencyCode.USD
    );

    assertThatThrownBy(() -> budgetValidator.validateRequest(req))
        .isInstanceOf(CustomLogicException.class)
        .hasMessageContaining("잘못된 요청입니다.");

  }

  @Test
  @DisplayName("validateRequest는 환전 여부가 false인데 환율이 있으면 400 BAD REQUEST가 발생한다")
  void validateRequest_with_CreateReq_should_return_400_2() throws Exception {

    BudgetCreateRequest req = new BudgetCreateRequest(
        BigDecimal.valueOf(100000),
        false,
        BigDecimal.valueOf(1393.7),
        CurrencyCode.USD
    );

    assertThatThrownBy(() -> budgetValidator.validateRequest(req))
        .isInstanceOf(CustomLogicException.class)
        .hasMessageContaining("잘못된 요청입니다.");

  }

  @Test
  @DisplayName("validateRequest는 유효한 BudgetUpdateRequest 검증한다")
  void validateRequest_verify_BudgetUpdateRequest() throws Exception {

    BudgetUpdateRequest req = genBudgetUpdateReq();

    assertThatCode(() -> budgetValidator.validateRequest(req))
        .doesNotThrowAnyException();

  }

  @Test
  @DisplayName("validateRequest는 환전 여부가 true인데 환율이 없으면 400 BAD REQUEST가 발생한다")
  void validateRequest_with_UpdateReq_should_return_400_1() throws Exception {

    BudgetUpdateRequest req = new BudgetUpdateRequest(
        BigDecimal.valueOf(80000),
        true,
        null
    );

    assertThatThrownBy(() -> budgetValidator.validateRequest(req))
        .isInstanceOf(CustomLogicException.class)
        .hasMessageContaining("잘못된 요청입니다.");

  }

  @Test
  @DisplayName("validateRequest는 환전 여부가 false인데 환율이 있으면 400 BAD REQUEST가 발생한다")
  void validateRequest_with_UpdateReq_should_return_400_2() throws Exception {

    BudgetUpdateRequest req = new BudgetUpdateRequest(
        BigDecimal.valueOf(80000),
        false,
        BigDecimal.valueOf(1393.7)
    );

    assertThatThrownBy(() -> budgetValidator.validateRequest(req))
        .isInstanceOf(CustomLogicException.class)
        .hasMessageContaining("잘못된 요청입니다.");

  }

  @Test
  @DisplayName("validateRequest는 유효한 BudgetAddRequest 검증한다")
  void validateRequest_verify_BudgetAddRequest() throws Exception {

    BudgetAddRequest req = genBudgetAddReq();

    assertThatCode(() -> budgetValidator.validateRequest(req))
        .doesNotThrowAnyException();

  }

  @Test
  @DisplayName("validateRequest는 환전 여부가 true인데 환율이 없으면 400 BAD REQUEST가 발생한다")
  void validateRequest_with_AddReq_should_return_400_1() throws Exception {

    BudgetAddRequest req = new BudgetAddRequest(
        BigDecimal.valueOf(50000),
        true,
        null
    );

    assertThatThrownBy(() -> budgetValidator.validateRequest(req))
        .isInstanceOf(CustomLogicException.class)
        .hasMessageContaining("잘못된 요청입니다.");

  }

  @Test
  @DisplayName("validateRequest는 환전 여부가 false인데 환율이 있으면 400 BAD REQUEST가 발생한다")
  void validateRequest_with_AddReq_should_return_400_2() throws Exception {

    BudgetAddRequest req = new BudgetAddRequest(
        BigDecimal.valueOf(50000),
        false,
        BigDecimal.valueOf(1393.7)
    );

    assertThatThrownBy(() -> budgetValidator.validateRequest(req))
        .isInstanceOf(CustomLogicException.class)
        .hasMessageContaining("잘못된 요청입니다.");

  }
}