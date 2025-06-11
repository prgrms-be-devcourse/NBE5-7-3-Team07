package com.luckyseven.backend.domain.settlements.app;

import com.luckyseven.backend.domain.expense.dto.ExpenseRequest;
import com.luckyseven.backend.domain.expense.entity.Expense;
import com.luckyseven.backend.domain.expense.repository.ExpenseRepository;
import com.luckyseven.backend.domain.member.entity.Member;
import com.luckyseven.backend.domain.member.service.MemberService;
import com.luckyseven.backend.domain.settlements.dao.SettlementRepository;
import com.luckyseven.backend.domain.settlements.dao.SettlementSpecification;
import com.luckyseven.backend.domain.settlements.dto.SettlementCreateRequest;
import com.luckyseven.backend.domain.settlements.dto.SettlementResponse;
import com.luckyseven.backend.domain.settlements.dto.SettlementSearchCondition;
import com.luckyseven.backend.domain.settlements.dto.SettlementUpdateRequest;
import com.luckyseven.backend.domain.settlements.entity.Settlement;
import com.luckyseven.backend.domain.settlements.util.SettlementMapper;
import com.luckyseven.backend.sharedkernel.exception.CustomLogicException;
import com.luckyseven.backend.sharedkernel.exception.ExceptionCode;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SettlementService {

  private final SettlementRepository settlementRepository;
  private final MemberService memberService;
  private final ExpenseRepository expenseRepository;

  public void createAllSettlements(ExpenseRequest request, Member payer, Expense expense) {
    List<Long> settlerIds = request.settlerId();
    List<Long> nonPayerIds = getNonPayerIds(settlerIds, request.payerId);

    BigDecimal shareAmount = calculateShareAmount(request.amount, settlerIds.size());

    Map<Long, Member> settlerMap = getSettlerMap(nonPayerIds);

    List<Settlement> settlements = nonPayerIds.stream()
        .map(settlerId -> createSettlement(settlerId, payer, expense, shareAmount, settlerMap))
        .toList();

    settlementRepository.saveAll(settlements);
  }

  private List<Long> getNonPayerIds(List<Long> settlerIds, Long payerId) {
    return settlerIds.stream()
        .filter(id -> !id.equals(payerId))
        .toList();
  }

  private BigDecimal calculateShareAmount(BigDecimal amount, int totalMembers) {
    return amount.divide(BigDecimal.valueOf(totalMembers), RoundingMode.HALF_UP);
  }

  private Map<Long, Member> getSettlerMap(List<Long> nonPayerIds) {
    List<Member> settlers = memberService.findMembersByIds(nonPayerIds);
    return settlers.stream()
        .collect(Collectors.toMap(Member::getId, Function.identity()));
  }

  private Settlement createSettlement(Long settlerId, Member payer, Expense expense,
      BigDecimal shareAmount, Map<Long, Member> settlerMap) {
    Member settler = settlerMap.get(settlerId);
    validateSettler(settler);

    SettlementCreateRequest createRequest = SettlementMapper.toSettlementCreateRequest(
        expense, payer.getId(), settlerId, shareAmount
    );

    return SettlementMapper.fromSettlementCreateRequest(
        createRequest,
        settler,
        payer,
        expense
    );
  }

  private void validateSettler(Member settler) {
    if (settler == null) {
      throw new CustomLogicException(ExceptionCode.NOT_TEAM_MEMBER);
    }
  }

  @Transactional(readOnly = true)
  public SettlementResponse readSettlement(Long id) {
    Settlement settlement = findSettlementOrThrow(id);
    return SettlementMapper.toSettlementResponse(settlement);
  }

  /**
   * 검색 조건에 따른 정산 목록을 페이지네이션하여 조회합니다. teamId는 필수 condition 각 속성은 선택
   *
   * @param teamId    팀 ID (필터링 조건)
   * @param condition 지불자 ID, 정산자 ID, 지출 ID, 정산 상태를 포함하는 검색 조건
   * @param pageable  페이지네이션 정보
   * @return 검색 조건에 맞는 정산 응답 객체들이 담긴 Page 객체
   * @throws CustomLogicException teamId가 null인 경우 (BAD_REQUEST)
   */
  @Transactional(readOnly = true)
  public Page<SettlementResponse> readSettlementPage(Long teamId,
      SettlementSearchCondition condition, Pageable pageable) {
    Specification<Settlement> specification = SettlementSpecification.createSpecification(teamId,
        condition);
    Page<Settlement> settlementPage = settlementRepository.findAll(specification, pageable);

    return settlementPage.map(SettlementMapper::toSettlementResponse);
  }

  @Transactional
  public SettlementResponse updateSettlement(Long id, SettlementUpdateRequest request) {

    Settlement settlement = findSettlementOrThrow(id);
    Member settler = request.settlerId() != null ?
        memberService.findMemberOrThrow(request.settlerId()) : null;
    Member payer = request.settlerId() != null ?
        memberService.findMemberOrThrow(request.payerId()) : null;
    Expense expense = getExpense(request);

    settlement.update(request.amount(), settler, payer, expense, request.isSettled());
    return SettlementMapper.toSettlementResponse(settlementRepository.save(settlement));
  }

  private Expense getExpense(SettlementUpdateRequest request) {
    return expenseRepository.findById(request.expenseId())
        .orElseThrow(() -> new CustomLogicException(ExceptionCode.EXPENSE_NOT_FOUND));
  }

  @Transactional
  public SettlementResponse settleSettlement(Long id) {
    Settlement settlement = findSettlementOrThrow(id);
    settlement.convertSettled();
    return SettlementMapper.toSettlementResponse(settlementRepository.save(settlement));
  }

  public Settlement findSettlementOrThrow(Long id) {
    Settlement settlement = settlementRepository.findWithSettlerAndPayerById(id).orElseThrow(
        () -> new CustomLogicException(ExceptionCode.SETTLEMENT_NOT_FOUND)
    );
    return settlement;
  }
}
