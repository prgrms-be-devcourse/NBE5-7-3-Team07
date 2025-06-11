package com.luckyseven.backend.domain.expense.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.luckyseven.backend.domain.budget.entity.Budget;
import com.luckyseven.backend.domain.budget.entity.CurrencyCode;
import com.luckyseven.backend.domain.expense.dto.CreateExpenseResponse;
import com.luckyseven.backend.domain.expense.dto.ExpenseBalanceResponse;
import com.luckyseven.backend.domain.expense.dto.ExpenseRequest;
import com.luckyseven.backend.domain.expense.dto.ExpenseResponse;
import com.luckyseven.backend.domain.expense.dto.ExpenseUpdateRequest;
import com.luckyseven.backend.domain.expense.entity.Expense;
import com.luckyseven.backend.domain.expense.enums.ExpenseCategory;
import com.luckyseven.backend.domain.expense.enums.PaymentMethod;
import com.luckyseven.backend.domain.expense.mapper.ExpenseMapper;
import com.luckyseven.backend.domain.expense.repository.ExpenseRepository;
import com.luckyseven.backend.domain.member.entity.Member;
import com.luckyseven.backend.domain.member.service.MemberService;
import com.luckyseven.backend.domain.settlements.app.SettlementService;
import com.luckyseven.backend.domain.settlements.dao.SettlementRepository;
import com.luckyseven.backend.domain.team.entity.Team;
import com.luckyseven.backend.domain.team.repository.TeamRepository;
import com.luckyseven.backend.sharedkernel.cache.CacheEvictService;
import com.luckyseven.backend.sharedkernel.dto.PageResponse;
import com.luckyseven.backend.sharedkernel.exception.CustomLogicException;
import com.luckyseven.backend.sharedkernel.exception.ExceptionCode;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class ExpenseServiceTest {

  @Mock
  private TeamRepository teamRepository;

  @Mock
  private MemberService memberService;

  @Mock
  private ExpenseRepository expenseRepository;

  @Mock
  private SettlementService settlementService;

  @Mock
  private CacheEvictService cacheEvictService;

  @Mock
  private SettlementRepository settlementRepository;

  @InjectMocks
  private ExpenseService expenseService;

  private Team team;
  private Budget budget;
  private Member payer;
  private Member settler1;
  private Member settler2;

  @BeforeEach
  void setUp() {
    budget = Budget.builder()
        .balance(new BigDecimal("100000.00"))
        .foreignBalance(new BigDecimal("100000.00"))
        .foreignCurrency(CurrencyCode.USD)
        .avgExchangeRate(new BigDecimal("1.00"))
        .build();
    payer = Member.builder()
        .email("dldldldl@naver.com")
        .nickname("하하하하하")
        .password("1234")
        .build();

    settler1 = Member.builder()
        .id(10L)
        .email("settler1@example.com")
        .nickname("정산자1")
        .password("abcd1234")
        .build();

    settler2 = Member.builder()
        .id(20L)
        .email("settler2@example.com")
        .nickname("정산자2")
        .password("qwer5678")
        .build();

    team = Team.builder()
        .id(1L)
        .name("테스트팀")
        .teamCode("ABC123")
        .teamPassword("password")
        .leader(payer)
        .budget(budget)
        .build();

  }

  @Nested
  @DisplayName("지출 등록 테스트")
  class SaveExpenseTests {

    @Test
    @DisplayName("지출 등록 성공")
    void success() {
      // given
      ExpenseRequest request = ExpenseRequest.builder()
          .payerId(1L)
          .settlerId(List.of(10L, 20L))
          .description("럭키비키즈 점심 식사")
          .amount(new BigDecimal("10000.00"))
          .category(ExpenseCategory.MEAL)
          .paymentMethod(PaymentMethod.CASH)
          .build();

      when(expenseRepository.save(any(Expense.class)))
          .thenAnswer(invocation -> invocation.getArgument(0));
      when(teamRepository.findTeamWithBudget(1L))
          .thenReturn(Optional.of(team));
      when(memberService.findMemberOrThrow(1L)).thenReturn(payer);

      // when
      CreateExpenseResponse response = expenseService.saveExpense(1L, request);

      // then
      ArgumentCaptor<Expense> captor = ArgumentCaptor.forClass(Expense.class);
      verify(expenseRepository).save(captor.capture());
      Expense saved = captor.getValue();
      assertThat(saved.getDescription()).isEqualTo(request.description);

      BigDecimal expectedBalance = new BigDecimal("100000.00").subtract(request.amount);
      assertThat(budget.getBalance()).isEqualByComparingTo(expectedBalance);
      assertThat(response.balance).isEqualByComparingTo(expectedBalance);
    }

    @Nested
    class ExceptionCases {

      @Test
      @DisplayName("존재하지 않는 팀")
      void teamNotFound_throwsException() {
        when(teamRepository.findTeamWithBudget(999L)).thenReturn(Optional.empty());
        ExpenseRequest request = ExpenseRequest.builder()
            .payerId(1L)
            .settlerId(List.of(10L, 20L))
            .description("변종된 럭키비키즈 나쁜 점심 식사")
            .amount(new BigDecimal("1000.00"))
            .category(ExpenseCategory.MEAL)
            .paymentMethod(PaymentMethod.CASH)
            .build();

        assertThatThrownBy(() -> expenseService.saveExpense(999L, request))
            .isInstanceOf(CustomLogicException.class)
            .extracting("exceptionCode")
            .isEqualTo(ExceptionCode.TEAM_NOT_FOUND);
      }

      @Test
      @DisplayName("예산보다 큰 지출 금액")
      void insufficientBalance_throwsException() {
        when(teamRepository.findTeamWithBudget(1L)).thenReturn(Optional.of(team));
        ExpenseRequest request = ExpenseRequest.builder()
            .payerId(1L)
            .description("럭키비키즈 팀 배부르게 식사")
            .amount(new BigDecimal("1000000.00"))
            .category(ExpenseCategory.MEAL)
            .paymentMethod(PaymentMethod.CARD)
            .build();

        assertThatThrownBy(() -> expenseService.saveExpense(1L, request))
            .isInstanceOf(CustomLogicException.class)
            .extracting("exceptionCode")
            .isEqualTo(ExceptionCode.INSUFFICIENT_BALANCE);
      }
    }

    @Test
    @DisplayName("지출 등록 성공 및 정산 생성 호출")
    void success_and_createSettlements() {
      // given
      ExpenseRequest request = ExpenseRequest.builder()
          .payerId(1L)
          .settlerId(List.of(10L, 20L))
          .description("럭키비키즈 점심 식사")
          .amount(new BigDecimal("50000.00"))
          .category(ExpenseCategory.MEAL)
          .paymentMethod(PaymentMethod.CASH)
          .build();

      when(expenseRepository.save(any(Expense.class)))
          .thenAnswer(invocation -> invocation.getArgument(0));
      when(teamRepository.findTeamWithBudget(1L))
          .thenReturn(Optional.of(team));
      when(memberService.findMemberOrThrow(1L))
          .thenReturn(payer);

      // when
      CreateExpenseResponse response = expenseService.saveExpense(1L, request);

      // then – expense 저장 검증
      ArgumentCaptor<Expense> expenseCaptor = ArgumentCaptor.forClass(Expense.class);
      verify(expenseRepository).save(expenseCaptor.capture());
      Expense savedExpense = expenseCaptor.getValue();
      assertThat(savedExpense.getDescription()).isEqualTo(request.description);

      // then – 예산 차감 검증
      BigDecimal expectedBalance = new BigDecimal("100000.00").subtract(request.amount);
      assertThat(budget.getBalance()).isEqualByComparingTo(expectedBalance);
      assertThat(response.balance).isEqualByComparingTo(expectedBalance);

      verify(settlementService).createAllSettlements(eq(request), eq(payer), eq(savedExpense));
    }

  }

  @Nested
  @DisplayName("지출 조회 테스트")
  class GetExpenseTests {

    @Test
    @DisplayName("지출 조회 성공")
    void success() {
      Expense expense = Expense.builder()
          .description("럭키비키즈 점심 식사")
          .amount(new BigDecimal("50000.00"))
          .category(ExpenseCategory.MEAL)
          .paymentMethod(PaymentMethod.CARD)
          .payer(payer)
          .team(team)
          .build();

      when(expenseRepository.findByIdWithPayer(expense.getId())).thenReturn(
          Optional.of(expense));

      ExpenseResponse response = expenseService.getExpense(expense.getId());

      assertThat(response.description).isEqualTo("럭키비키즈 점심 식사");
      assertThat(response.amount).isEqualByComparingTo(new BigDecimal("50000"));
      assertThat(response.category).isEqualTo(ExpenseCategory.MEAL);
      assertThat(response.paymentMethod).isEqualTo(PaymentMethod.CARD);
      verify(expenseRepository).findByIdWithPayer(expense.getId());
    }


    @Test
    @DisplayName("존재하지 않는 지출 조회 시 예외 발생")
    void notFound_throwsException() {
      // given
      Long expenseId = 999L;
      when(expenseRepository.findByIdWithPayer(expenseId)).thenReturn(Optional.empty());

      // when & then
      assertThatThrownBy(() -> expenseService.getExpense(expenseId))
          .isInstanceOf(CustomLogicException.class)
          .extracting("exceptionCode")
          .isEqualTo(ExceptionCode.EXPENSE_NOT_FOUND);
    }
  }


  @Nested
  @DisplayName("지출 수정 테스트")
  class UpdateExpenseTests {

    @Test
    @DisplayName("지출 금액 증가 수정 성공")
    void increaseAmountSuccess() {

      // given
      ExpenseUpdateRequest request = ExpenseUpdateRequest.builder()
          .description("잘못 계산해서 수정한 럭키비키즈 점심 식사")
          .amount(new BigDecimal("70000.00"))
          .category(ExpenseCategory.MEAL)
          .build();

      Expense original = Expense.builder()
          .description("원래 럭키비키즈 점심 식사")
          .amount(new BigDecimal("50000.00"))
          .category(ExpenseCategory.MEAL)
          .payer(payer)
          .team(team)
          .build();

      when(expenseRepository.findWithTeamAndBudgetById(1L)).thenReturn(Optional.of(original));

      // when
      CreateExpenseResponse response = expenseService.updateExpense(1L, request);

      // then
      assertThat(original.getDescription()).isEqualTo(request.description);
      assertThat(original.getAmount()).isEqualByComparingTo(request.amount);
      BigDecimal expectedDelta = request.amount.subtract(new BigDecimal("50000.00"));
      BigDecimal expectedBalance = new BigDecimal("100000.00").subtract(expectedDelta);
      assertThat(budget.getBalance()).isEqualByComparingTo(expectedBalance);
      assertThat(response.balance).isEqualByComparingTo(expectedBalance);
    }

    @Test
    @DisplayName("지출 금액 감소 수정 성공")
    void decreaseAmountSuccess() {
      // given
      ExpenseUpdateRequest request = ExpenseUpdateRequest.builder()
          .description("업데이트된 점심 식사")
          .amount(new BigDecimal("30000.00"))
          .category(ExpenseCategory.MEAL)
          .build();

      Expense original = Expense.builder()
          .description("원본 점심 식사")
          .amount(new BigDecimal("50000.00"))
          .category(ExpenseCategory.MEAL)
          .payer(payer)
          .team(team)
          .build();
      when(expenseRepository.findWithTeamAndBudgetById(1L)).thenReturn(Optional.of(original));

      // when
      CreateExpenseResponse response = expenseService.updateExpense(1L, request);

      // then
      BigDecimal expectedDelta = request.amount.subtract(new BigDecimal("50000.00"));
      BigDecimal expectedBalance = new BigDecimal("100000.00").subtract(expectedDelta);
      assertThat(budget.getBalance()).isEqualByComparingTo(expectedBalance);
      assertThat(response.balance).isEqualByComparingTo(expectedBalance);
    }

    @Test
    @DisplayName("존재하지 않는 지출")
    void expenseNotFound_throwsException() {
      when(expenseRepository.findWithTeamAndBudgetById(999L)).thenReturn(Optional.empty());
      ExpenseUpdateRequest request = ExpenseUpdateRequest.builder()
          .description("없는 지출 수정")
          .amount(new BigDecimal("1000.00"))
          .category(ExpenseCategory.MEAL)
          .build();

      assertThatThrownBy(() -> expenseService.updateExpense(999L, request))
          .isInstanceOf(CustomLogicException.class)
          .extracting("exceptionCode")
          .isEqualTo(ExceptionCode.EXPENSE_NOT_FOUND);
    }

    @Test
    @DisplayName("예산 부족으로 수정 실패")
    void insufficientBalance_throwsException() {
      // given: 예산 10만, 원본 지출 5만, 업데이트 요청 200만
      ExpenseUpdateRequest request = ExpenseUpdateRequest.builder()
          .description("너무 많이 먹었는데 예산보다 많은 지출 수정")
          .amount(new BigDecimal("200000.00"))
          .category(ExpenseCategory.MEAL)
          .build();

      Expense original = Expense.builder()
          .description("럭키비키즈 원래 점심 식사")
          .amount(new BigDecimal("50000.00"))
          .category(ExpenseCategory.MEAL)
          .payer(payer)
          .team(team)
          .build();
      when(expenseRepository.findWithTeamAndBudgetById(1L)).thenReturn(Optional.of(original));

      // when & then
      assertThatThrownBy(() -> expenseService.updateExpense(1L, request))
          .isInstanceOf(CustomLogicException.class)
          .extracting("exceptionCode")
          .isEqualTo(ExceptionCode.INSUFFICIENT_BALANCE);
    }
  }

  @Nested
  @DisplayName("지출 삭제 테스트")
  class DeleteExpenseTests {

    @Test
    @DisplayName("지출 삭제 성공")
    void success() {
      // given: 기존 지출 금액 10만원, 삭제할 지출 3만원
      Expense expense = Expense.builder()
          .amount(new BigDecimal("30000.00"))
          .team(team)
          .build();
      when(expenseRepository.findWithTeamAndBudgetById(1L)).thenReturn(Optional.of(expense));

      // when
      ExpenseBalanceResponse response = expenseService.deleteExpense(1L);

      // then
      // 예산 13만원으로 증가
      BigDecimal expectedBalance = new BigDecimal("130000.00");
      assertThat(team.getBudget().getBalance()).isEqualByComparingTo(expectedBalance);
      assertThat(response.balance).isEqualByComparingTo(expectedBalance);

      verify(expenseRepository).delete(expense);
    }

    @Test
    @DisplayName("존재하지 않는 지출 삭제 시 예외 발생")
    void expenseNotFound_throwsException() {
      when(expenseRepository.findWithTeamAndBudgetById(999L)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> expenseService.deleteExpense(999L))
          .isInstanceOf(CustomLogicException.class)
          .extracting("exceptionCode")
          .isEqualTo(ExceptionCode.EXPENSE_NOT_FOUND);
    }
  }

  @Nested
  @DisplayName("지출 리스트 조회 테스트")
  class GetListExpenseTests {

    @Test
    @DisplayName("지출 리스트 정상 반환")
    void success() {
      // given
      Pageable pageable = PageRequest.of(0, 10);

      // 1) Expense 엔티티 생성
      List<Expense> expenses = List.of(
          Expense.builder()
              .description("점심 식사")
              .amount(new BigDecimal("10000.00"))
              .category(ExpenseCategory.MEAL)
              .paymentMethod(PaymentMethod.CASH)
              .payer(payer)
              .team(team)
              .build(),
          Expense.builder()
              .description("저녁 식사")
              .amount(new BigDecimal("15000.00"))
              .category(ExpenseCategory.MEAL)
              .paymentMethod(PaymentMethod.CARD)
              .payer(payer)
              .team(team)
              .build()
      );

      List<ExpenseResponse> responseDtos = expenses.stream()
          .map(ExpenseMapper::toExpenseResponse)
          .toList();
      Page<ExpenseResponse> page = new PageImpl<>(responseDtos, pageable, responseDtos.size());

      when(teamRepository.existsById(1L)).thenReturn(true);
      when(expenseRepository.findResponsesByTeamId(1L, pageable)).thenReturn(page);

      PageResponse<ExpenseResponse> result = expenseService.getListExpense(1L, pageable);

      // then
      assertThat(result.getContent()).hasSize(2);
      assertThat(result.getPage()).isEqualTo(0);
      assertThat(result.getSize()).isEqualTo(10);
      assertThat(result.getTotalElements()).isEqualTo(2);
      assertThat(result.getTotalPages()).isEqualTo(1);

      ExpenseResponse first = result.getContent().get(0);
      assertThat(first.description).isEqualTo("점심 식사");
      assertThat(first.amount).isEqualByComparingTo(new BigDecimal("10000.00"));
    }


    @Test
    @DisplayName("존재하지 않는 팀으로 조회 시 예외 발생")
    void teamNotFound() {
      Pageable pageable = PageRequest.of(0, 5);
      when(teamRepository.existsById(999L)).thenReturn(false);

      assertThatThrownBy(() -> expenseService.getListExpense(999L, pageable))
          .isInstanceOf(CustomLogicException.class)
          .extracting("exceptionCode")
          .isEqualTo(ExceptionCode.TEAM_NOT_FOUND);
    }

  }
}
