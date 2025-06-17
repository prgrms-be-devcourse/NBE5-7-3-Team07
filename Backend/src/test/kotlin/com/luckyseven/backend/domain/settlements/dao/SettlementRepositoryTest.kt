package com.luckyseven.backend.domain.settlements.dao

import com.luckyseven.backend.domain.budget.entity.Budget
import com.luckyseven.backend.domain.budget.entity.CurrencyCode
import com.luckyseven.backend.domain.expense.entity.Expense
import com.luckyseven.backend.domain.expense.enums.ExpenseCategory
import com.luckyseven.backend.domain.expense.enums.PaymentMethod
import com.luckyseven.backend.domain.member.entity.Member
import com.luckyseven.backend.domain.settlement.dao.SettlementRepository
import com.luckyseven.backend.domain.settlement.dao.SettlementSpecification
import com.luckyseven.backend.domain.settlement.entity.Settlement
import com.luckyseven.backend.domain.team.entity.Team
import com.luckyseven.backend.sharedkernel.entity.BaseEntity
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.data.domain.PageRequest
import org.springframework.data.jpa.domain.Specification
import org.springframework.test.context.ActiveProfiles
import java.math.BigDecimal
import java.time.LocalDateTime


@DataJpaTest
@ActiveProfiles("test")
class SettlementRepositoryTest {

    @Autowired
    private lateinit var settlementRepository: SettlementRepository

    @PersistenceContext
    private lateinit var entityManager: EntityManager

    private lateinit var team1: Team
    private lateinit var team2: Team
    private lateinit var settler1: Member
    private lateinit var settler2: Member
    private lateinit var payer1: Member
    private lateinit var payer2: Member
    private lateinit var expense1: Expense
    private lateinit var expense2: Expense

    fun setDate(t: BaseEntity) {
        t.createdAt = LocalDateTime.now()
        t.updatedAt = LocalDateTime.now()
    }

    @BeforeEach
    fun setUp() {
        // 멤버 생성
        settler1 = Member(
            email = "settler1@example.com",
            password = "password1",
            nickname = "정산자1"
        )
        setDate(settler1)
        settler2 = Member(
            email = "settler2@example.com",
            password = "password2",
            nickname = "정산자2"
        )
        setDate(settler2)
        payer1 = Member(
            email = "payer1@example.com",
            password = "password3",
            nickname = "지불자1"
        )
        setDate(payer1)
        payer2 = Member(
            email = "payer2@example.com",
            password = "password4",
            nickname = "지불자2"
        )
        setDate(payer2)

        // 멤버 엔티티 저장
        entityManager.persist(settler1)
        entityManager.persist(settler2)
        entityManager.persist(payer1)
        entityManager.persist(payer2)

        // Budget 객체 생성
        val budget1 = Budget(
            totalAmount = BigDecimal("100000.00"),
            setBy = settler1.id!!,
            balance = BigDecimal("100000.00"),
            foreignBalance = BigDecimal("0.00"),
            foreignCurrency = CurrencyCode.USD,
            avgExchangeRate = BigDecimal("1300")
        )
        setDate(budget1)

        val budget2 = Budget(
            totalAmount = BigDecimal("50000.00"),
            setBy = payer1.id!!,
            balance = BigDecimal("50000.00"),
            foreignBalance = BigDecimal("0.00"),
            foreignCurrency = CurrencyCode.USD,
            avgExchangeRate = BigDecimal("1300")
        )
        setDate(budget2)

        entityManager.persist(budget1)
        entityManager.persist(budget2)

        // 팀 생성
        team1 = Team(
            name = "team1",
            teamCode = "CODE123",
            teamPassword = "pass123",
            leader = settler1,
            budget = budget1
        )
        setDate(team1)

        team2 = Team(
            name = "team2",
            teamCode = "CODE456",
            teamPassword = "pass456",
            leader = payer1,
            budget = budget2
        )
        setDate(team2)

        // 팀 엔티티 저장
        entityManager.persist(team1)
        entityManager.persist(team2)

        // Expense 객체 생성
        expense1 = Expense(
            amount = BigDecimal.valueOf(1000),
            description = "ex1",
            team = team1,
            payer = payer1,
            category = ExpenseCategory.MEAL,
            paymentMethod = PaymentMethod.CARD
        )
        setDate(expense1)

        expense2 = Expense(
            amount = BigDecimal.valueOf(1000),
            description = "ex2",
            team = team2,
            payer = payer2,
            category = ExpenseCategory.MEAL,
            paymentMethod = PaymentMethod.CARD
        )
        setDate(expense2)

        // 비용 엔티티 저장
        entityManager.persist(expense1)
        entityManager.persist(expense2)

        // 변경사항 반영
        entityManager.flush()

        // Settlement 객체 생성 및 저장
        for (i in 0 until 20) {
            val settlement = Settlement(
                amount = BigDecimal.valueOf(1000),
                settler = if (i < 5) settler1 else settler2,
                payer = if (i < 10) payer1 else payer2,
                expense = if (i < 15) expense1 else expense2
            )
            setDate(settlement)
            if (i % 2 == 0) {
                settlement.convertSettled()
            }
            settlementRepository.save(settlement)
        }
    }

    @Nested
    @DisplayName("실제 레포지토리 테스트")
    inner class ActualRepositoryTests {
        @Test
        @DisplayName("팀_명세 - 특정 팀의 정산 목록을 조회한다")
        fun findAllWithTeamSpecification() {
            // given
            val team1Spec = Specification.where(
                SettlementSpecification.hasTeamId(team1.id)
            )
            val pageRequest = PageRequest.of(0, 10)

            // when
            val result = settlementRepository.findAll(team1Spec, pageRequest)

            // then
            result.content shouldHaveSize 10
            result.totalElements shouldBe 15
            result.content.all { it?.expense?.team == team1 } shouldBe true
        }

        @Test
        @DisplayName("정산_명세 - 특정 지출의 정산 목록을 조회한다")
        fun findAllWithExpenseSpecification() {
            // given
            val expenseSpec = Specification.where(
                SettlementSpecification.hasExpenseId(expense1.id)
            )
            val pageRequest = PageRequest.of(0, 10)

            // when
            val result = settlementRepository.findAll(expenseSpec, pageRequest)

            // then
            result.content shouldHaveSize 10
            result.totalElements shouldBe 15
            result.content.all { it?.expense == expense1 } shouldBe true
        }

        @Test
        @DisplayName("정산완료여부_명세 - 정산 완료된 목록을 조회한다")
        fun findAllWithSettledSpecification() {
            // given
            val settledSpec = Specification.where(
                SettlementSpecification.isSettled(true)
            )
            val pageRequest = PageRequest.of(0, 10)

            // when
            val result = settlementRepository.findAll(settledSpec, pageRequest)

            // then
            result.content shouldHaveSize 10
            result.totalElements shouldBe 10
            result.content.all { it?.isSettled!! } shouldBe true
        }

        @Test
        @DisplayName("지불자_명세 - 특정 지불자의 정산 목록을 조회한다")
        fun findAllWithPayerSpecification() {
            // given
            val payerSpec = Specification.where(
                SettlementSpecification.hasPayerId(payer1.id)
            )
            val pageRequest = PageRequest.of(0, 10)

            // when
            val result = settlementRepository.findAll(payerSpec, pageRequest)

            // then
            result.content shouldHaveSize 10
            result.totalElements shouldBe 10
            result.content.all { it?.payer == payer1 } shouldBe true
        }

        @Test
        @DisplayName("정산자_명세 - 특정 정산자의 정산 목록을 조회한다")
        fun findAllWithSettlerSpecification() {
            // given
            val settlerSpec = Specification.where(
                SettlementSpecification.hasSettlerId(settler1.id)
            )
            val pageRequest = PageRequest.of(0, 10)

            // when
            val result = settlementRepository.findAll(settlerSpec, pageRequest)

            // then
            result.content shouldHaveSize 5
            result.totalElements shouldBe 5
            result.content.all { it?.settler == settler1 } shouldBe true
        }
    }

    @Test
    @DisplayName("정산 완료 여부 명세 - Mockk")
    fun testSettledSpecificationWithMock() {
        // given
        val settledSpec = Specification.where(
            SettlementSpecification.isSettled(true)
        )
        val pageRequest = PageRequest.of(0, 10)

        // when
        val result = settlementRepository.findAll(settledSpec, pageRequest)

        // then
        result.content shouldHaveSize 10
        result.totalElements shouldBe 10
        result.content.all { it?.isSettled!! } shouldBe true
    }

    @Test
    @DisplayName("팀 명세 - Mockk")
    fun testTeamSpecificationWithMock() {
        // given
        val team1Spec = Specification.where(
            SettlementSpecification.hasTeamId(team1.id)
        )
        val pageRequest = PageRequest.of(0, 10)

        // when
        val result = settlementRepository.findAll(team1Spec, pageRequest)

        // then
        result.content shouldHaveSize 10
        result.totalElements shouldBe 15
        result.content.all { it?.expense?.team == team1 } shouldBe true
    }
}
