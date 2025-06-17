package com.luckyseven.backend.domain.settlements.app

import com.luckyseven.backend.domain.expense.entity.Expense
import com.luckyseven.backend.domain.expense.enums.ExpenseCategory
import com.luckyseven.backend.domain.expense.enums.PaymentMethod
import com.luckyseven.backend.domain.expense.repository.ExpenseRepository
import com.luckyseven.backend.domain.member.entity.Member
import com.luckyseven.backend.domain.member.service.MemberService
import com.luckyseven.backend.domain.settlement.app.SettlementService
import com.luckyseven.backend.domain.settlement.dao.SettlementRepository
import com.luckyseven.backend.domain.settlement.dto.SettlementSearchCondition
import com.luckyseven.backend.domain.settlement.dto.SettlementUpdateRequest
import com.luckyseven.backend.domain.settlement.entity.Settlement
import com.luckyseven.backend.domain.team.dto.TeamMemberDto
import com.luckyseven.backend.domain.team.entity.Team
import com.luckyseven.backend.domain.team.service.TeamMemberService
import com.luckyseven.backend.sharedkernel.exception.CustomLogicException
import com.luckyseven.backend.sharedkernel.exception.ExceptionCode
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import jakarta.persistence.EntityManager
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.Specification
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*
import java.util.stream.Stream

@ExtendWith(MockKExtension::class)
class SettlementServiceTest {

    @MockK
    private lateinit var settlementRepository: SettlementRepository

    @MockK
    private lateinit var memberService: MemberService

    @MockK
    private lateinit var teamMemberService: TeamMemberService

    @MockK
    private lateinit var expenseRepository: ExpenseRepository

    @MockK
    private lateinit var em: EntityManager

    @InjectMockKs
    private lateinit var settlementService: SettlementService

    private lateinit var team: Team
    private lateinit var settlement: Settlement
    private lateinit var settler: Member
    private lateinit var payer: Member
    private lateinit var expense: Expense

    @BeforeEach
    fun setUp() {
        settler = Member(id = 1L, email = "123@123", password = "password", nickname = "nickname")
        payer = Member(id = 2L, email = "456@456", password = "password", nickname = "nickname2")
        team = Team(
            id = 1L,
            name = "team",
            teamCode = "1234",
            teamPassword = "1234",
            leader = payer
        )
        expense = Expense(
            id = 1L,
            amount = BigDecimal.valueOf(1000),
            description = "식비",
            category = ExpenseCategory.MEAL,
            paymentMethod = PaymentMethod.CARD,
            payer = payer,
            team = team
        )
        settlement = Settlement(
            id = 1L,
            amount = BigDecimal.valueOf(1000),
            settler = settler,
            payer = payer,
            expense = expense,
            isSettled = false
        )
        settlement.createdAt = LocalDateTime.now()
        settlement.updatedAt = LocalDateTime.now()
    }

    @Test
    @DisplayName("정산완료")
    fun convertSettled_ShouldUpdateSettlementStatus() {
        // given
        every { settlementRepository.findWithSettlerAndPayerById(any()) } returns settlement
        every { settlementRepository.save(any()) } returns settlement

        // when
        val updated = settlementService.settleSettlement(1L)

        // then
        updated.isSettled shouldBe true
        verify { settlementRepository.save(settlement) }
    }

    @Test
    @DisplayName("정산 조회")
    fun findSettlement_ShouldReturnSettlement() {
        // given
        every { settlementRepository.findWithSettlerAndPayerById(any()) } returns settlement

        // when
        val found = settlementService.readSettlement(1L)

        // then
        found shouldNotBe null
        found.amount shouldBe BigDecimal.valueOf(1000)
        found.settlerId shouldBe settler.id
        found.payerId shouldBe payer.id
        found.expenseId shouldBe expense.id
    }

    @Test
    @DisplayName("정산 수정")
    fun updateSettlement_ShouldUpdateSettlement() {
        // given
        every { settlementRepository.findWithSettlerAndPayerById(any()) } returns settlement
        every { settlementRepository.save(any()) } returns settlement
        every { memberService.findMemberOrThrow(any()) } returns settler andThen payer
        every { expenseRepository.findById(any()) } returns Optional.of(expense)

        val newAmount = BigDecimal.valueOf(2000)
        val request = SettlementUpdateRequest(
            amount = newAmount,
            payerId = payer.id,
            settlerId = settler.id,
            expenseId = expense.id,
            isSettled = false,
            settlementId = settlement.id!!
        )

        // when
        val updated = settlementService.updateSettlement(1L, request)

        // then
        updated shouldNotBe null
        updated.amount shouldBe newAmount
        updated.settlerId shouldBe settler.id
        updated.payerId shouldBe payer.id
        updated.expenseId shouldBe expense.id
    }

    @Test
    @DisplayName("정산목록조회_페이지네이션_명세")
    fun findAllSettlements_ShouldReturnAllSettlements() {
        // given
        val settlements = listOf(settlement)
        val mockPage = PageImpl(settlements)
        every {
            settlementRepository.findAll(
                any<Specification<Settlement>>(),
                any<Pageable>()
            )
        } returns mockPage

        val condition = SettlementSearchCondition(
            payerId = 1L,
            settlerId = 1L,
            expenseId = 1L,
            isSettled = false
        )

        // when
        val result = settlementService.readSettlementPage(1L, condition, PageRequest.of(0, 10))

        // then
        result.content.size shouldBe 1
        result.content.all { it.amount == BigDecimal.valueOf(1000) } shouldBe true
        result.content.all { !it.isSettled } shouldBe true
        verify { settlementRepository.findAll(any<Specification<Settlement>>(), any<Pageable>()) }
    }

    @Test
    @DisplayName("존재하지 않는 정산 조회 시 예외 발생")
    fun readSettlement_WithNonExistingId_ShouldThrowException() {
        // given
        val nonExistingId = 999L
        every { settlementRepository.findWithSettlerAndPayerById(nonExistingId) } returns null

        // when and then
        val exception = shouldThrow<CustomLogicException> {
            settlementService.readSettlement(nonExistingId)
        }
        exception.exceptionCode shouldBe ExceptionCode.SETTLEMENT_NOT_FOUND
    }

    @Test
    @DisplayName("존재하지 않는 정산 수정 시 예외 발생")
    fun updateSettlement_WithNonExistingId_ShouldThrowException() {
        // given
        val nonExistingId = 999L
        every { settlementRepository.findWithSettlerAndPayerById(nonExistingId) } returns null

        val request = SettlementUpdateRequest(
            amount = BigDecimal.valueOf(2000),
            settlementId = 1,
            settlerId = 1,
            payerId = 1,
            expenseId = 1,
            isSettled = false
        )

        // when and then
        val exception = shouldThrow<CustomLogicException> {
            settlementService.updateSettlement(nonExistingId, request)
        }
        exception.exceptionCode shouldBe ExceptionCode.SETTLEMENT_NOT_FOUND
    }

    @Test
    @DisplayName("존재하지 않는 정산 완료 처리 시 예외 발생")
    fun settleSettlement_WithNonExistingId_ShouldThrowException() {
        // given
        val nonExistingId = 999L
        every { settlementRepository.findWithSettlerAndPayerById(nonExistingId) } returns null

        // when and then
        val exception = shouldThrow<CustomLogicException> {
            settlementService.settleSettlement(nonExistingId)
        }
        exception.exceptionCode shouldBe ExceptionCode.SETTLEMENT_NOT_FOUND
    }

    @Test
    @DisplayName("팀 내 정산 집계 조회")
    fun getSettlementsAggregation_ShouldReturnAggregation() {
        // given
        val teamId = 1L
        val member1 =
            Member(id = 1L, email = "member1@test.com", password = "password", nickname = "member1")
        val member2 =
            Member(id = 2L, email = "member2@test.com", password = "password", nickname = "member2")
        val member3 =
            Member(id = 3L, email = "member3@test.com", password = "password", nickname = "member3")


        // 팀 멤버 목록 설정
        every { teamMemberService.getTeamMemberByTeamId(teamId) } returns listOf(
            TeamMemberDto(
                id = 1L,
                teamId = 2L,
                teamName = "team1",
                memberId = member1.id,
                memberNickName = member1.nickname,
                memberEmail = member1.email,
                role = "leader"
            ),
            TeamMemberDto(
                id = 2L,
                teamId = 2L,
                teamName = "team1",
                memberId = member2.id,
                memberNickName = member2.nickname,
                memberEmail = member2.email,
                role = "member"
            ),
            TeamMemberDto(
                id = 3L,
                teamId = 2L,
                teamName = "team1",
                memberId = member3.id,
                memberNickName = member3.nickname,
                memberEmail = member3.email,
                role = "member"
            )
        )

        // 정산 데이터 설정
        val settlement1 = Settlement(
            id = 1L,
            amount = BigDecimal.valueOf(1000),
            settler = member1,
            payer = member2,
            expense = expense,
            isSettled = false
        )
        val settlement2 = Settlement(
            id = 2L,
            amount = BigDecimal.valueOf(500),
            settler = member2,
            payer = member1,
            expense = expense,
            isSettled = false
        )
        val settlement3 = Settlement(
            id = 3L,
            amount = BigDecimal.valueOf(300),
            settler = member3,
            payer = member1,
            expense = expense,
            isSettled = false
        )

        val settlementStream = Stream.of(settlement1, settlement2, settlement3)
        every { settlementRepository.findAllByTeamId(teamId) } returns settlementStream
        every { em.clear() } returns Unit

        // when
        val result = settlementService.getSettlementsAggregation(teamId)

        // then
        result.aggregations.isNotEmpty() shouldBe true
        result.aggregations.find { it.from == member1.id && it.to == member2.id }?.amount shouldBe BigDecimal.valueOf(
            500
        )
        result.aggregations.find { it.from == member3.id && it.to == member1.id }?.amount shouldBe BigDecimal.valueOf(
            300
        )
        result.aggregations.any { it.amount > BigDecimal.ZERO } shouldBe true
    }

    @Test
    @DisplayName("멤버 간 정산 일괄처리")
    fun settleBetweenMembers_ShouldMarkSettlementsAsSettled() {
        // given
        val teamId = 1L
        val fromMemberId = 1L
        val toMemberId = 2L

        val settlement1 = Settlement(
            id = 1L,
            amount = BigDecimal.valueOf(1000),
            settler = settler,
            payer = payer,
            expense = expense,
            isSettled = false
        )
        val settlement2 = Settlement(
            id = 2L,
            amount = BigDecimal.valueOf(500),
            settler = settler,
            payer = payer,
            expense = expense,
            isSettled = false
        )

        val settlementStream = Stream.of(settlement1, settlement2)
        every {
            settlementRepository.findAssociatedNotSettled(
                teamId,
                fromMemberId,
                toMemberId
            )
        } returns settlementStream
        // when
        settlementService.settleBetweenMembers(teamId, fromMemberId, toMemberId)

        // then
        // 모든 정산이 완료 상태로 변경되었는지 확인
        settlement1.isSettled shouldBe true
        settlement2.isSettled shouldBe true
    }
}
