package com.luckyseven.backend.domain.settlements.app

import com.luckyseven.backend.domain.expense.dto.ExpenseRequest
import com.luckyseven.backend.domain.expense.entity.Expense
import com.luckyseven.backend.domain.expense.repository.ExpenseRepository
import com.luckyseven.backend.domain.member.entity.Member
import com.luckyseven.backend.domain.member.service.MemberService
import com.luckyseven.backend.domain.settlements.dao.SettlementRepository
import com.luckyseven.backend.domain.settlements.dao.SettlementSpecification
import com.luckyseven.backend.domain.settlements.dto.SettlementResponse
import com.luckyseven.backend.domain.settlements.dto.SettlementSearchCondition
import com.luckyseven.backend.domain.settlements.dto.SettlementUpdateRequest
import com.luckyseven.backend.domain.settlements.entity.Settlement
import com.luckyseven.backend.domain.settlements.util.SettlementMapper
import com.luckyseven.backend.sharedkernel.exception.CustomLogicException
import com.luckyseven.backend.sharedkernel.exception.ExceptionCode
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.math.RoundingMode

@Service
class SettlementService(
    private val settlementRepository: SettlementRepository,
    private val memberService: MemberService,
    private val expenseRepository: ExpenseRepository
) {
    fun createAllSettlements(request: ExpenseRequest, payer: Member, expense: Expense) {
        val settlerIds = request.settlerId()
        val nonPayerIds = getNonPayerIds(settlerIds, request.payerId())
        val shareAmount = calculateShareAmount(request.amount(), settlerIds.size)
        val settlerMap = getSettlerMap(nonPayerIds)
        val settlements = nonPayerIds.map { settlerId ->
            createSettlement(settlerId, payer, expense, shareAmount, settlerMap)
        }
        settlementRepository.saveAll(settlements)
    }

    private fun getNonPayerIds(settlerIds: List<Long>, payerId: Long): List<Long> =
        settlerIds.filter { it != payerId }

    private fun calculateShareAmount(amount: BigDecimal, totalMembers: Int): BigDecimal =
        amount.divide(BigDecimal.valueOf(totalMembers.toLong()), RoundingMode.HALF_UP)

    private fun getSettlerMap(nonPayerIds: List<Long>): Map<Long, Member> =
        memberService.findMembersByIds(nonPayerIds).associateBy { it.id }

    private fun createSettlement(
        settlerId: Long,
        payer: Member,
        expense: Expense,
        shareAmount: BigDecimal,
        settlerMap: Map<Long, Member>
    ): Settlement {
        val settler =
            settlerMap[settlerId] ?: throw CustomLogicException(ExceptionCode.NOT_TEAM_MEMBER)
        val createRequest = SettlementMapper.toSettlementCreateRequest(
            expense = expense,
            payerId = payer.id,
            settlerId = settlerId,
            shareAmount = shareAmount
        )
        return SettlementMapper.fromSettlementCreateRequest(
            createRequest,
            settler,
            payer,
            expense
        )
    }

    @Transactional(readOnly = true)
    fun readSettlement(id: Long): SettlementResponse {
        val settlement = findSettlementOrThrow(id)
        return SettlementMapper.toSettlementResponse(settlement)
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
    fun readSettlementPage(
        teamId: Long?,
        condition: SettlementSearchCondition,
        pageable: Pageable
    ): Page<SettlementResponse> {
        val specification: Specification<Settlement> =
            SettlementSpecification.createSpecification(teamId, condition)
        val settlementPage = settlementRepository.findAll(specification, pageable)
        return settlementPage.map(SettlementMapper::toSettlementResponse)
    }

    @Transactional
    fun updateSettlement(id: Long, request: SettlementUpdateRequest): SettlementResponse {
        val settlement = findSettlementOrThrow(id)
        val settler = request.settlerId?.let { memberService.findMemberOrThrow(it) }
        val payer = request.payerId?.let { memberService.findMemberOrThrow(it) }
        val expense = getExpense(request)
        settlement.update(request.amount, settler, payer, expense, request.isSettled)
        return SettlementMapper.toSettlementResponse(settlementRepository.save(settlement))
    }

    private fun getExpense(request: SettlementUpdateRequest): Expense =
        expenseRepository.findById(request.expenseId)
            .orElseThrow { CustomLogicException(ExceptionCode.EXPENSE_NOT_FOUND) }

    @Transactional
    fun settleSettlement(id: Long): SettlementResponse {
        val settlement = findSettlementOrThrow(id)
        settlement.convertSettled()
        return SettlementMapper.toSettlementResponse(settlementRepository.save(settlement))
    }

    fun findSettlementOrThrow(id: Long): Settlement =
        settlementRepository.findWithSettlerAndPayerById(id)
            ?: throw CustomLogicException(ExceptionCode.SETTLEMENT_NOT_FOUND)
}

