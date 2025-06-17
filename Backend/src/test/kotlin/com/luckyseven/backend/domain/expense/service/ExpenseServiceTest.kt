package com.luckyseven.backend.domain.expense.service

import com.luckyseven.backend.domain.budget.entity.Budget
import com.luckyseven.backend.domain.budget.entity.CurrencyCode
import com.luckyseven.backend.domain.expense.cache.CacheEvictService
import com.luckyseven.backend.domain.expense.dto.ExpenseUpdateRequest
import com.luckyseven.backend.domain.expense.entity.Expense
import com.luckyseven.backend.domain.expense.enums.ExpenseCategory
import com.luckyseven.backend.domain.expense.enums.PaymentMethod
import com.luckyseven.backend.domain.expense.mapper.ExpenseMapper
import com.luckyseven.backend.domain.expense.repository.ExpenseRepository
import com.luckyseven.backend.domain.expense.util.ExpenseTestUtils
import com.luckyseven.backend.domain.member.entity.Member
import com.luckyseven.backend.domain.member.repository.MemberRepository
import com.luckyseven.backend.domain.settlement.app.SettlementService
import com.luckyseven.backend.domain.team.cache.TeamDashboardCacheService
import com.luckyseven.backend.domain.team.entity.Team
import com.luckyseven.backend.domain.team.repository.TeamRepository
import com.luckyseven.backend.sharedkernel.exception.CustomLogicException
import com.luckyseven.backend.sharedkernel.exception.ExceptionCode
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.justRun
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import java.math.BigDecimal

@ExtendWith(MockKExtension::class)
internal class ExpenseServiceTest {

    @MockK
    private lateinit var teamRepository: TeamRepository

    @MockK
    private lateinit var expenseRepository: ExpenseRepository

    @MockK
    private lateinit var settlementService: SettlementService

    @MockK
    private lateinit var cacheEvictService: CacheEvictService

    @MockK
    private lateinit var memberRepository: MemberRepository

    @InjectMockKs
    private lateinit var expenseService: ExpenseService

    @MockK
    private lateinit var teamDashboardCacheService: TeamDashboardCacheService

    private lateinit var team: Team
    private lateinit var budget: Budget
    private lateinit var payer: Member
    private lateinit var settler1: Member
    private lateinit var settler2: Member

    @BeforeEach
    fun setUp() {
        payer = Member(
            id = 1L,
            email = "dldldldl@naver.com",
            password = "1234",
            nickname = "유한"
        )
        settler1 = Member(
            id = 10L,
            email = "settler1@example.com",
            password = "abcd1234",
            nickname = "유한1"
        )
        settler2 = Member(
            id = 20L,
            email = "settler2@example.com",
            password = "qwer5678",
            nickname = "유한2"
        )
        budget = Budget(
            id = null,
            team = null,
            totalAmount = BigDecimal("100000.00"),
            setBy = 1L,
            balance = BigDecimal("100000.00"),
            foreignBalance = BigDecimal("100000.00"),
            foreignCurrency = CurrencyCode.USD,
            avgExchangeRate = BigDecimal("1.00")
        )
        team = Team(
            id = 1L,
            name = "테스트팀",
            teamCode = "ABC123",
            teamPassword = "password",
            leader = payer,
            budget = budget
        )
        budget.setTeam(team)

        justRun { cacheEvictService.evictByPrefix(any(), any()) }
        justRun { teamDashboardCacheService.evictTeamDashboardCache(any()) }
    }

    @Nested
    @DisplayName("지출 등록 테스트")
    inner class SaveExpenseTests {

        @Test
        @DisplayName("지출 등록 성공")
        fun success() {
            // given
            val request = ExpenseTestUtils.buildRequest(
                description = "럭키비키즈 점심 식사",
                amount = BigDecimal("10000.00"),
                settlerIds = listOf(10L, 20L)
            )

            every { expenseRepository.save(any<Expense>()) } answers { firstArg() }
            every { teamRepository.findTeamWithBudget(1L) } returns team
            every { memberRepository.findByIdOrNull(1L) } returns payer
            justRun { settlementService.createAllSettlements(any(), any(), any()) }

            // when
            val response = expenseService.saveExpense(1L, request)

            // then
            verify { expenseRepository.save(match { it.description == request.description }) }
            verify { cacheEvictService.evictByPrefix(any(), any()) }
            verify {
                settlementService.createAllSettlements(
                    request,
                    payer,
                    match { savedExpense ->
                        savedExpense.description == request.description &&
                                savedExpense.amount == request.amount &&
                                savedExpense.payer.id == payer.id
                    }
                )
            }

            val expectedBalance = BigDecimal("100000.00") - request.amount
            assertThat(budget.balance).isEqualByComparingTo(expectedBalance)
            assertThat(response.balance).isEqualByComparingTo(expectedBalance)
        }

        @Nested
        inner class ExceptionCases {

            @Test
            @DisplayName("존재하지 않는 팀")
            fun teamNotFound_throwsException() {
                every { teamRepository.findTeamWithBudget(999L) } returns null
                val request = ExpenseTestUtils.buildRequest(
                    description = "변종된 럭키비키즈 나쁜 점심 식사",
                    amount = BigDecimal("1000.00"),
                    settlerIds = listOf(10L, 20L)
                )

                val exception = assertThrows<CustomLogicException> {
                    expenseService.saveExpense(999L, request)
                }
                assertThat(exception.exceptionCode).isEqualTo(ExceptionCode.TEAM_NOT_FOUND)
            }

            @Test
            @DisplayName("예산보다 큰 지출 금액")
            fun insufficientBalance_throwsException() {
                every { teamRepository.findTeamWithBudget(1L) } returns team
                every { memberRepository.findByIdOrNull(1L) } returns payer

                val request = ExpenseTestUtils.buildRequest(
                    description = "럭키비키즈 팀 배부르게 식사",
                    amount = BigDecimal("1000000.00"),
                    paymentMethod = PaymentMethod.CARD,
                    settlerIds = listOf(10L)
                )

                val exception = assertThrows<CustomLogicException> {
                    expenseService.saveExpense(1L, request)
                }
                assertThat(exception.exceptionCode).isEqualTo(ExceptionCode.INSUFFICIENT_BALANCE)
            }
        }

        @Test
        @DisplayName("지출 등록 성공 및 정산 생성 호출")
        fun success_and_createSettlements() {
            val request = ExpenseTestUtils.buildRequest(
                description = "럭키비키즈 점심 식사",
                amount = BigDecimal("50000.00"),
                settlerIds = listOf(10L, 20L)
            )

            every { expenseRepository.save(any<Expense>()) } answers { firstArg() }
            every { teamRepository.findTeamWithBudget(1L) } returns team
            every { memberRepository.findByIdOrNull(1L) } returns payer
            justRun { settlementService.createAllSettlements(any(), any(), any()) }

            val response = expenseService.saveExpense(1L, request)

            verify { expenseRepository.save(any()) }
            verify {
                settlementService.createAllSettlements(
                    request,
                    payer,
                    match { savedExpense ->
                        savedExpense.description == request.description &&
                                savedExpense.amount == request.amount &&
                                savedExpense.payer.id == payer.id
                    }
                )
            }
            verify { cacheEvictService.evictByPrefix(any(), any()) }

            val expectedBalance = BigDecimal("100000.00") - request.amount
            assertThat(budget.balance).isEqualByComparingTo(expectedBalance)
            assertThat(response.balance).isEqualByComparingTo(expectedBalance)
        }
    }

    @Nested
    @DisplayName("지출 조회 테스트")
    inner class GetExpenseTests {

        @Test
        @DisplayName("지출 조회 성공")
        fun success() {
            val expense = Expense(
                id = 1L,
                description = "럭키비키즈 점심 식사",
                amount = BigDecimal("50000.00"),
                category = ExpenseCategory.MEAL,
                paymentMethod = PaymentMethod.CARD,
                payer = payer,
                team = team
            )
            every { expenseRepository.findByIdWithPayer(1L) } returns expense

            val response = expenseService.getExpense(1L)

            assertThat(response.description).isEqualTo(expense.description)
            assertThat(response.amount).isEqualByComparingTo(expense.amount)
            assertThat(response.category).isEqualTo(expense.category)
            assertThat(response.paymentMethod).isEqualTo(expense.paymentMethod)
            verify { expenseRepository.findByIdWithPayer(1L) }
        }

        @Test
        @DisplayName("존재하지 않는 지출 조회 시 예외 발생")
        fun notFound_throwsException() {
            every { expenseRepository.findByIdWithPayer(999L) } returns null

            val exception = assertThrows<CustomLogicException> {
                expenseService.getExpense(999L)
            }
            assertThat(exception.exceptionCode).isEqualTo(ExceptionCode.EXPENSE_NOT_FOUND)
        }
    }

    @Nested
    @DisplayName("지출 수정 테스트")
    inner class UpdateExpenseTests {

        @Test
        @DisplayName("지출 금액 증가 수정 성공")
        fun increaseAmountSuccess() {
            val request = ExpenseUpdateRequest(
                description = "잘못 계산해서 수정한 럭키비키즈 점심 식사",
                amount = BigDecimal("70000.00"),
                category = ExpenseCategory.MEAL
            )
            val original = Expense(
                id = 1L,
                description = "원래 럭키비키즈 점심 식사",
                amount = BigDecimal("50000.00"),
                category = ExpenseCategory.MEAL,
                paymentMethod = PaymentMethod.CASH,
                payer = payer,
                team = team
            )
            every { expenseRepository.findWithTeamAndBudgetById(1L) } returns original

            val response = expenseService.updateExpense(1L, request)

            assertThat(original.description).isEqualTo(request.description)
            assertThat(original.amount).isEqualByComparingTo(request.amount)
            verify { cacheEvictService.evictByPrefix(any(), any()) }

            val expectedDelta = request.amount!! - BigDecimal("50000.00")
            val expectedBalance = BigDecimal("100000.00") - expectedDelta
            assertThat(budget.balance).isEqualByComparingTo(expectedBalance)
            assertThat(response.balance).isEqualByComparingTo(expectedBalance)
        }

        @Test
        @DisplayName("지출 금액 감소 수정 성공")
        fun decreaseAmountSuccess() {
            val request = ExpenseUpdateRequest(
                description = "업데이트된 점심 식사",
                amount = BigDecimal("30000.00"),
                category = ExpenseCategory.MEAL
            )
            val original = Expense(
                id = 1L,
                description = "원본 점심 식사",
                amount = BigDecimal("50000.00"),
                category = ExpenseCategory.MEAL,
                paymentMethod = PaymentMethod.CARD,
                payer = payer,
                team = team
            )
            every { expenseRepository.findWithTeamAndBudgetById(1L) } returns original

            val response = expenseService.updateExpense(1L, request)

            verify { cacheEvictService.evictByPrefix(any(), any()) }

            val expectedDelta = request.amount!! - BigDecimal("50000.00")
            val expectedBalance = BigDecimal("100000.00") - expectedDelta
            assertThat(budget.balance).isEqualByComparingTo(expectedBalance)
            assertThat(response.balance).isEqualByComparingTo(expectedBalance)
        }

        @Test
        @DisplayName("존재하지 않는 지출")
        fun expenseNotFound_throwsException() {
            every { expenseRepository.findWithTeamAndBudgetById(999L) } returns null
            val request = ExpenseUpdateRequest(
                description = "없는 지출 수정",
                amount = BigDecimal("1000.00"),
                category = ExpenseCategory.MEAL
            )

            val exception = assertThrows<CustomLogicException> {
                expenseService.updateExpense(999L, request)
            }
            assertThat(exception.exceptionCode).isEqualTo(ExceptionCode.EXPENSE_NOT_FOUND)
        }

        @Test
        @DisplayName("예산 부족으로 수정 실패")
        fun insufficientBalance_throwsException() {
            val request = ExpenseUpdateRequest(
                description = "너무 많이 먹었는데 예산보다 많은 지출 수정",
                amount = BigDecimal("200000.00"),
                category = ExpenseCategory.MEAL
            )
            val original = Expense(
                id = 1L,
                description = "럭키비키즈 원래 점심 식사",
                amount = BigDecimal("50000.00"),
                category = ExpenseCategory.MEAL,
                paymentMethod = PaymentMethod.CARD,
                payer = payer,
                team = team
            )
            every { expenseRepository.findWithTeamAndBudgetById(1L) } returns original

            val exception = assertThrows<CustomLogicException> {
                expenseService.updateExpense(1L, request)
            }
            assertThat(exception.exceptionCode).isEqualTo(ExceptionCode.INSUFFICIENT_BALANCE)
        }
    }

    @Nested
    @DisplayName("지출 삭제 테스트")
    inner class DeleteExpenseTests {

        @Test
        @DisplayName("지출 삭제 성공")
        fun success() {
            val expense = Expense(
                id = 1L,
                description = "삭제할 지출",
                amount = BigDecimal("30000.00"),
                category = ExpenseCategory.MEAL,
                paymentMethod = PaymentMethod.CASH,
                payer = payer,
                team = team
            )
            every { expenseRepository.findWithTeamAndBudgetById(1L) } returns expense
            justRun { expenseRepository.delete(expense) }

            val response = expenseService.deleteExpense(1L)

            val expectedBalance = BigDecimal("130000.00")
            assertThat(team.budget?.balance).isEqualByComparingTo(expectedBalance)
            assertThat(response.balance).isEqualByComparingTo(expectedBalance)

            verify { expenseRepository.delete(expense) }
            verify { cacheEvictService.evictByPrefix(any(), any()) }
        }

        @Test
        @DisplayName("존재하지 않는 지출 삭제 시 예외 발생")
        fun expenseNotFound_throwsException() {
            every { expenseRepository.findWithTeamAndBudgetById(999L) } returns null

            val exception = assertThrows<CustomLogicException> {
                expenseService.deleteExpense(999L)
            }
            assertThat(exception.exceptionCode).isEqualTo(ExceptionCode.EXPENSE_NOT_FOUND)
        }
    }

    @Nested
    @DisplayName("지출 리스트 조회 테스트")
    inner class GetListExpenseTests {

        @Test
        @DisplayName("지출 리스트 정상 반환")
        fun success() {
            val pageable: Pageable = PageRequest.of(0, 10)
            val responses = listOf(
                createExpense("점심 식사", BigDecimal("10000.00"), PaymentMethod.CASH),
                createExpense("저녁 식사", BigDecimal("15000.00"), PaymentMethod.CARD)
            ).map { ExpenseMapper.toExpenseResponse(it) }
            val page = PageImpl(responses, pageable, responses.size.toLong())

            every { teamRepository.existsById(1L) } returns true
            every { expenseRepository.findResponsesByTeamId(1L, pageable) } returns page

            val result = expenseService.getExpenses(1L, pageable)

            assertThat(result.content).hasSize(2)
            assertThat(result.page).isEqualTo(0)
            assertThat(result.size).isEqualTo(10)
            assertThat(result.totalElements).isEqualTo(2)
            assertThat(result.totalPages).isEqualTo(1)

            val first = result.content[0]
            assertThat(first.description).isEqualTo("점심 식사")
            assertThat(first.amount).isEqualByComparingTo(BigDecimal("10000.00"))
        }

        @Test
        @DisplayName("존재하지 않는 팀으로 조회 시 예외 발생")
        fun teamNotFound() {
            val pageable = PageRequest.of(0, 5)
            every { teamRepository.existsById(999L) } returns false

            val exception = assertThrows<CustomLogicException> {
                expenseService.getExpenses(999L, pageable)
            }
            assertThat(exception.exceptionCode).isEqualTo(ExceptionCode.TEAM_NOT_FOUND)
        }

        private fun createExpense(
            description: String,
            amount: BigDecimal,
            paymentMethod: PaymentMethod
        ) = Expense(
            id = null,
            description = description,
            amount = amount,
            category = ExpenseCategory.MEAL,
            paymentMethod = paymentMethod,
            payer = payer,
            team = team
        )
    }
}
