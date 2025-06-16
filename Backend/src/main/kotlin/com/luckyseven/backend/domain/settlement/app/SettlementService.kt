package com.luckyseven.backend.domain.settlement.app

import com.luckyseven.backend.domain.expense.dto.ExpenseRequest
import com.luckyseven.backend.domain.expense.entity.Expense
import com.luckyseven.backend.domain.expense.repository.ExpenseRepository
import com.luckyseven.backend.domain.member.entity.Member
import com.luckyseven.backend.domain.member.service.MemberService
import com.luckyseven.backend.domain.settlement.dao.SettlementRepository
import com.luckyseven.backend.domain.settlement.dao.SettlementSpecification
import com.luckyseven.backend.domain.settlement.dto.*
import com.luckyseven.backend.domain.settlement.entity.Settlement
import com.luckyseven.backend.domain.settlement.util.SettlementMapper
import com.luckyseven.backend.domain.team.service.TeamMemberService
import com.luckyseven.backend.sharedkernel.exception.CustomLogicException
import com.luckyseven.backend.sharedkernel.exception.ExceptionCode
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
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
    private val teamMemberService: TeamMemberService,
    private val expenseRepository: ExpenseRepository,
    @PersistenceContext
    private val em: EntityManager
) {
    fun createAllSettlements(request: ExpenseRequest, payer: Member, expense: Expense) {
        val settlerIds = request.settlerId
        val nonPayerIds = getNonPayerIds(settlerIds, request.payerId)
        val shareAmount = calculateShareAmount(request.amount, settlerIds.size)
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
        memberService.findMembersByIds(nonPayerIds).associateBy { it.id!! }

    fun createSettlement(
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
            payerId = payer.id!!,
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
        teamId: Long,
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
        val expense = request.expenseId?.let { getExpense(it) }
        settlement.update(request.amount, settler, payer, expense, request.isSettled)
        return SettlementMapper.toSettlementResponse(settlementRepository.save(settlement))
    }

    private fun getExpense(expenseId: Long): Expense =
        expenseRepository.findById(expenseId)
            .orElseThrow { CustomLogicException(ExceptionCode.EXPENSE_NOT_FOUND) }

    @Transactional
    fun settleSettlement(id: Long): SettlementResponse {
        val settlement = findSettlementOrThrow(id)
        settlement.convertSettled()
        return SettlementMapper.toSettlementResponse(settlementRepository.save(settlement))
    }

    private fun findSettlementOrThrow(id: Long): Settlement =
        settlementRepository.findWithSettlerAndPayerById(id)
            ?: throw CustomLogicException(ExceptionCode.SETTLEMENT_NOT_FOUND)

    @Transactional(readOnly = true)
    fun getSettlementsAggregation(teamId: Long): SettlementAggregationResponse {
        // TODO:집계 쿼리로 가져오기 or Stream 사용하기
        val memberIds = teamMemberService.getTeamMemberByTeamId(teamId).map { it -> it.id }
        val amountSum = Array(memberIds.size) { Array(memberIds.size) { BigDecimal.ZERO } }
        val memberIndexMap = memberIds.withIndex().associate { it.value to it.index }
        settlementRepository.findAllByTeamId(teamId).use { stream ->
            var count = 0
            stream.filter { it.settler.id != null && it.payer.id != null }
                .forEach {
                    val settlerIndex = memberIndexMap[it.settler.id]!!
                    val payerIndex = memberIndexMap[it.payer.id]!!
                    amountSum[settlerIndex][payerIndex] += it.amount
                    if (++count % 100 == 0) {
                        em.clear()
                    }
                }
        }
        val sumList = mutableListOf<SettlementMemberAggregationResponse>()
        for (i in 0 until memberIds.size) {
            for (j in i + 1 until memberIds.size) {
                if (amountSum[i][j] < amountSum[j][i]) {
                    sumList.add(
                        SettlementMemberAggregationResponse(
                            from = memberIds[j]!!,
                            to = memberIds[i]!!,
                            amount = amountSum[j][i] - amountSum[i][j]
                        )
                    )
                } else if (amountSum[i][j] > amountSum[j][i]) {
                    sumList.add(
                        SettlementMemberAggregationResponse(
                            from = memberIds[1]!!,
                            to = memberIds[j]!!,
                            amount = amountSum[i][j] - amountSum[j][i]
                        )
                    )
                }
            }
        }
        return SettlementAggregationResponse(sumList)
    }

    @Transactional
    fun settleBetweenMembers(teamId: Long, from: Long, to: Long) {
        //TODO: 쿼리튜닝으로 개선하기
        val settlements = settlementRepository.findAllByTeamId(teamId).filter { it ->
            (it.settler.id == from && it.payer.id == to) || (it.settler.id == to && it.payer.id == from)
        }.forEach { it.isSettled = true }

    }
}

