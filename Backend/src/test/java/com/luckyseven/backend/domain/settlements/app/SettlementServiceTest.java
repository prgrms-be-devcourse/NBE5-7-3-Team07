package com.luckyseven.backend.domain.settlements.app;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.luckyseven.backend.domain.expense.entity.Expense;
import com.luckyseven.backend.domain.expense.service.ExpenseService;
import com.luckyseven.backend.domain.member.entity.Member;
import com.luckyseven.backend.domain.member.service.MemberService;
import com.luckyseven.backend.domain.settlements.dao.SettlementRepository;
import com.luckyseven.backend.domain.settlements.dto.SettlementCreateRequest;
import com.luckyseven.backend.domain.settlements.dto.SettlementResponse;
import com.luckyseven.backend.domain.settlements.dto.SettlementSearchCondition;
import com.luckyseven.backend.domain.settlements.dto.SettlementUpdateRequest;
import com.luckyseven.backend.domain.settlements.entity.Settlement;
import com.luckyseven.backend.domain.team.entity.Team;
import com.luckyseven.backend.sharedkernel.exception.CustomLogicException;
import com.luckyseven.backend.sharedkernel.exception.ExceptionCode;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
class SettlementServiceTest {

  @Mock
  private SettlementRepository settlementRepository;
  @Mock
  private MemberService memberService;
  @Mock
  private ExpenseService expenseService;

  @InjectMocks
  private SettlementService settlementService;

  private Team team;
  private Settlement settlement;
  private Member settler;
  private Member payer;
  private Expense expense;

  @BeforeEach
  void setUp() {
    team = Team.builder().id(1L).build();
    settler = Member.builder().id(1L).email("123@123").build();
    payer = Member.builder().id(2L).email("456@456").build();
    expense = Expense.builder().amount(BigDecimal.valueOf(1000)).build();
    settlement = Settlement.builder()
        .amount(BigDecimal.valueOf(1000))
        .settler(settler)
        .payer(payer)
        .expense(expense)
        .build();
  }

  @Test
  @DisplayName("정산생성")
  void createSettlement_ShouldSaveSettlement() {
    //given
    when(settlementRepository.save(any(Settlement.class))).thenReturn(settlement);
    when(memberService.findMemberOrThrow(anyLong())).thenReturn(settler).thenReturn(payer);
    SettlementCreateRequest request = SettlementCreateRequest.builder()
        .amount(BigDecimal.valueOf(1000))
        .payerId(payer.getId())
        .settlerId(settler.getId())
        .expenseId(expense.getId())
        .build();

    //when
    SettlementResponse created = settlementService.createSettlement(request);

    //then
    assertNotNull(created);
    assertThat(created.amount).isEqualTo(BigDecimal.valueOf(1000));
    assertThat(created.settlerId()).isEqualTo(settler.getId());
    assertThat(created.payerId).isEqualTo(payer.getId());
    assertThat(created.expenseId).isEqualTo(expense.getId());
    assertFalse(created.isSettled);
  }

  @Test
  @DisplayName("정산롼료")
  void convertSettled_ShouldUpdateSettlementStatus() {
    //given
    when(settlementRepository.findWithSettlerAndPayerById(anyLong())).thenReturn(
        Optional.of(settlement));
    when(settlementRepository.save(any(Settlement.class))).thenReturn(settlement);

    SettlementResponse updated = settlementService.settleSettlement(1L);

    assertTrue(updated.isSettled);
    verify(settlementRepository).save(settlement);
  }

  @Test
  @DisplayName("정산 조회")
  void findSettlement_ShouldReturnSettlement() {
    //given
    when(settlementRepository.findWithSettlerAndPayerById(anyLong())).thenReturn(
        Optional.of(settlement));

    //when
    SettlementResponse found = settlementService.readSettlement(1L);

    //then
    assertNotNull(found);
    assertThat(found.amount).isEqualTo(BigDecimal.valueOf(1000));
    assertThat(found.settlerId()).isEqualTo(settler.getId());
    assertThat(found.payerId).isEqualTo(payer.getId());
    assertThat(found.expenseId).isEqualTo(expense.getId());
  }

  @Test
  @DisplayName("정산 수정")
  void updateSettlement_ShouldUpdateSettlement() {
    //given
    when(settlementRepository.findWithSettlerAndPayerById(anyLong())).thenReturn(
        Optional.of(settlement));
    when(settlementRepository.save(any(Settlement.class))).thenReturn(settlement);

    BigDecimal newAmount = BigDecimal.valueOf(2000);
    SettlementUpdateRequest request = SettlementUpdateRequest.builder()
        .amount(newAmount)
        .payerId(payer.getId())
        .settlerId(settler.getId())
        .expenseId(expense.getId())
        .build();

    //when
    SettlementResponse updated = settlementService.updateSettlement(1L, request);

    //then
    assertNotNull(updated);
    assertThat(updated.amount).isEqualTo(newAmount);
    assertThat(updated.settlerId()).isEqualTo(settler.getId());
    assertThat(updated.payerId).isEqualTo(payer.getId());
    assertThat(updated.expenseId).isEqualTo(expense.getId());
  }

  @Test
  @DisplayName("정산목록조회_페이지네이션_명세")
  void findAllSettlements_ShouldReturnAllSettlements() {
    //given
    List<Settlement> settlements = new ArrayList<>();
    settlements.add(settlement);
    Page<Settlement> mockPage = new PageImpl<>(settlements);
    when(settlementRepository.findAll(any(Specification.class), any(Pageable.class)))
        .thenReturn(mockPage);

    SettlementSearchCondition condition = SettlementSearchCondition.builder()
        .payerId(1L)
        .settlerId(1L)
        .expenseId(1L)
        .isSettled(false)
        .build();

    //when
    Page<SettlementResponse> result = settlementService.readSettlementPage(1L, condition,
        PageRequest.of(0, 10));

    //then
    assertThat(result.getContent().size()).isEqualTo(1);
    assertThat(result.getContent()).allMatch(s -> s.amount.equals(BigDecimal.valueOf(1000)));
    assertThat(result.getContent()).allMatch(s -> !s.isSettled);
    verify(settlementRepository).findAll(any(Specification.class), any(Pageable.class));
  }


  @Test
  @DisplayName("존재하지 않는 정산 조회 시 예외 발생")
  void readSettlement_WithNonExistingId_ShouldThrowException() {
    // given
    Long nonExistingId = 999L;
    when(settlementRepository.findWithSettlerAndPayerById(nonExistingId)).thenReturn(
        Optional.empty());

    // when & then
    assertThatThrownBy(() -> settlementService.readSettlement(nonExistingId))
        .isInstanceOf(CustomLogicException.class)
        .hasFieldOrPropertyWithValue("exceptionCode", ExceptionCode.SETTLEMENT_NOT_FOUND);
  }

  @Test
  @DisplayName("존재하지 않는 정산 수정 시 예외 발생")
  void updateSettlement_WithNonExistingId_ShouldThrowException() {
    // given
    Long nonExistingId = 999L;
    when(settlementRepository.findWithSettlerAndPayerById(nonExistingId)).thenReturn(
        Optional.empty());

    SettlementUpdateRequest request = SettlementUpdateRequest.builder()
        .amount(BigDecimal.valueOf(2000))
        .build();

    // when & then
    assertThatThrownBy(() -> settlementService.updateSettlement(nonExistingId, request))
        .isInstanceOf(CustomLogicException.class)
        .hasFieldOrPropertyWithValue("exceptionCode", ExceptionCode.SETTLEMENT_NOT_FOUND);
  }

  @Test
  @DisplayName("존재하지 않는 정산 완료 처리 시 예외 발생")
  void settleSettlement_WithNonExistingId_ShouldThrowException() {
    // given
    Long nonExistingId = 999L;
    when(settlementRepository.findWithSettlerAndPayerById(nonExistingId)).thenReturn(
        Optional.empty());

    // when & then
    assertThatThrownBy(() -> settlementService.settleSettlement(nonExistingId))
        .isInstanceOf(CustomLogicException.class)
        .hasFieldOrPropertyWithValue("exceptionCode", ExceptionCode.SETTLEMENT_NOT_FOUND);
  }
}