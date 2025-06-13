package com.luckyseven.backend.domain.expense.service

import com.luckyseven.backend.domain.budget.entity.Budget
import com.luckyseven.backend.domain.budget.entity.CurrencyCode
import com.luckyseven.backend.domain.expense.cache.CacheEvictService
import com.luckyseven.backend.domain.expense.dto.ExpenseRequest
import com.luckyseven.backend.domain.expense.dto.ExpenseUpdateRequest
import com.luckyseven.backend.domain.expense.entity.Expense
import com.luckyseven.backend.domain.expense.enums.ExpenseCategory
import com.luckyseven.backend.domain.expense.enums.PaymentMethod
import com.luckyseven.backend.domain.expense.mapper.ExpenseMapper
import com.luckyseven.backend.domain.expense.repository.ExpenseRepository
import com.luckyseven.backend.domain.member.entity.Member
import com.luckyseven.backend.domain.member.service.MemberService
import com.luckyseven.backend.domain.settlement.app.SettlementService
import com.luckyseven.backend.domain.team.entity.Team
import com.luckyseven.backend.domain.team.repository.TeamRepository
import com.luckyseven.backend.sharedkernel.exception.CustomLogicException
import com.luckyseven.backend.sharedkernel.exception.ExceptionCode
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.any
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import java.math.BigDecimal

@ExtendWith(MockitoExtension::class)
internal class ExpenseServiceTest {

    @Mock
    private lateinit var teamRepository: TeamRepository

    @Mock
    private lateinit var memberService: MemberService

    @Mock
    private lateinit var expenseRepository: ExpenseRepository

    @Mock
    private lateinit var settlementService: SettlementService

    @Mock
    private lateinit var cacheEvictService: CacheEvictService

    @InjectMocks
    private lateinit var expenseService: ExpenseService

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
    }

    @Nested
    @DisplayName("지출 등록 테스트")
    internal inner class SaveExpenseTests {

        @Test
        @DisplayName("지출 등록 성공")
        fun success() {
            // given
            val request = ExpenseRequest(
                description = "럭키비키즈 점심 식사",
                amount = BigDecimal("10000.00"),
                category = ExpenseCategory.MEAL,
                payerId = 1L,
                paymentMethod = PaymentMethod.CASH,
                settlerId = mutableListOf(10L, 20L)
            )

            whenever(expenseRepository.save(any<Expense>()))
                .thenAnswer { it.getArgument<Expense>(0) }
            whenever(teamRepository.findTeamWithBudget(1L)).thenReturn(team)
            whenever(memberService.findMemberOrThrow(1L)).thenReturn(payer)

            // when
            val response = expenseService.saveExpense(1L, request)

            // then
            val captor = argumentCaptor<Expense>()
            verify(expenseRepository).save(captor.capture())
            val saved = captor.firstValue

            assertThat(saved.description).isEqualTo(request.description)

            val expectedBalance = BigDecimal("100000.00") - request.amount
            assertThat(budget.balance).isEqualByComparingTo(expectedBalance)
            assertThat(response.balance).isEqualByComparingTo(expectedBalance)
        }

        @Nested
        internal inner class ExceptionCases {

            @Test
            @DisplayName("존재하지 않는 팀")
            fun teamNotFound_throwsException() {
                // given
                whenever(teamRepository.findTeamWithBudget(999L)).thenReturn(null)
                val request = ExpenseRequest(
                    description = "변종된 럭키비키즈 나쁜 점심 식사",
                    amount = BigDecimal("1000.00"),
                    category = ExpenseCategory.MEAL,
                    payerId = 1L,
                    paymentMethod = PaymentMethod.CASH,
                    settlerId = mutableListOf(10L, 20L)
                )

                // when & then
                val exception = assertThrows<CustomLogicException> {
                    expenseService.saveExpense(999L, request)
                }
                assertThat(exception.exceptionCode).isEqualTo(ExceptionCode.TEAM_NOT_FOUND)
            }

            @Test
            @DisplayName("예산보다 큰 지출 금액")
            fun insufficientBalance_throwsException() {
                // given
                whenever(teamRepository.findTeamWithBudget(1L)).thenReturn(team)
                val request = ExpenseRequest(
                    description = "럭키비키즈 팀 배부르게 식사",
                    amount = BigDecimal("1000000.00"),
                    category = ExpenseCategory.MEAL,
                    payerId = 1L,
                    paymentMethod = PaymentMethod.CARD,
                    settlerId = mutableListOf(10L)
                )

                // when & then
                val exception = assertThrows<CustomLogicException> {
                    expenseService.saveExpense(1L, request)
                }
                assertThat(exception.exceptionCode).isEqualTo(ExceptionCode.INSUFFICIENT_BALANCE)
            }
        }

        @Test
        @DisplayName("지출 등록 성공 및 정산 생성 호출")
        fun success_and_createSettlements() {
            // given
            val request = ExpenseRequest(
                description = "럭키비키즈 점심 식사",
                amount = BigDecimal("50000.00"),
                category = ExpenseCategory.MEAL,
                payerId = 1L,
                paymentMethod = PaymentMethod.CASH,
                settlerId = mutableListOf(10L, 20L)
            )

            whenever(expenseRepository.save(any<Expense>()))
                .thenAnswer { it.getArgument<Expense>(0) }
            whenever(teamRepository.findTeamWithBudget(1L)).thenReturn(team)
            whenever(memberService.findMemberOrThrow(1L)).thenReturn(payer)

            // when
            val response = expenseService.saveExpense(1L, request)

            // then
            val expenseCaptor = argumentCaptor<Expense>()
            verify(expenseRepository).save(expenseCaptor.capture())
            val savedExpense = expenseCaptor.firstValue

            assertThat(savedExpense.description).isEqualTo(request.description)

            val expectedBalance = BigDecimal("100000.00") - request.amount
            assertThat(budget.balance).isEqualByComparingTo(expectedBalance)
            assertThat(response.balance).isEqualByComparingTo(expectedBalance)

            // Todo: 수정하기
//            verify(settlementService).createAllSettlements(
//                eq(request),
//                eq(payer),
//                eq(savedExpense)
//            )
        }
    }

    @Nested
    @DisplayName("지출 조회 테스트")
    internal inner class GetExpenseTests {

        @Test
        @DisplayName("지출 조회 성공")
        fun success() {
            // given
            val expense = Expense(
                id = 1L,
                description = "럭키비키즈 점심 식사",
                amount = BigDecimal("50000.00"),
                category = ExpenseCategory.MEAL,
                paymentMethod = PaymentMethod.CARD,
                payer = payer,
                team = team
            )
            whenever(expenseRepository.findByIdWithPayer(1L)).thenReturn(expense)

            // when
            val response = expenseService.getExpense(1L)

            // then
            assertThat(response.description).isEqualTo(expense.description)
            assertThat(response.amount).isEqualByComparingTo(expense.amount)
            assertThat(response.category).isEqualTo(expense.category)
            assertThat(response.paymentMethod).isEqualTo(expense.paymentMethod)
            verify(expenseRepository).findByIdWithPayer(1L)
        }

        @Test
        @DisplayName("존재하지 않는 지출 조회 시 예외 발생")
        fun notFound_throwsException() {
            // given
            val expenseId = 999L
            whenever(expenseRepository.findByIdWithPayer(expenseId)).thenReturn(null)

            // when & then
            val exception = assertThrows<CustomLogicException> {
                expenseService.getExpense(expenseId)
            }
            assertThat(exception.exceptionCode).isEqualTo(ExceptionCode.EXPENSE_NOT_FOUND)
        }
    }

    @Nested
    @DisplayName("지출 수정 테스트")
    internal inner class UpdateExpenseTests {

        @Test
        @DisplayName("지출 금액 증가 수정 성공")
        fun increaseAmountSuccess() {
            // given
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
            whenever(expenseRepository.findWithTeamAndBudgetById(1L)).thenReturn(original)

            // when
            val response = expenseService.updateExpense(1L, request)

            // then
            assertThat(original.description).isEqualTo(request.description)
            assertThat(original.amount).isEqualByComparingTo(request.amount)

            val expectedDelta = request.amount!! - BigDecimal("50000.00")
            val expectedBalance = BigDecimal("100000.00") - expectedDelta
            assertThat(budget.balance).isEqualByComparingTo(expectedBalance)
            assertThat(response.balance).isEqualByComparingTo(expectedBalance)
        }

        @Test
        @DisplayName("지출 금액 감소 수정 성공")
        fun decreaseAmountSuccess() {
            // given
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
            whenever(expenseRepository.findWithTeamAndBudgetById(1L)).thenReturn(original)

            // when
            val response = expenseService.updateExpense(1L, request)

            // then
            val expectedDelta = request.amount!! - BigDecimal("50000.00")
            val expectedBalance = BigDecimal("100000.00") - expectedDelta
            assertThat(budget.balance).isEqualByComparingTo(expectedBalance)
            assertThat(response.balance).isEqualByComparingTo(expectedBalance)
        }

        @Test
        @DisplayName("존재하지 않는 지출")
        fun expenseNotFound_throwsException() {
            // given
            whenever(expenseRepository.findWithTeamAndBudgetById(999L)).thenReturn(null)
            val request = ExpenseUpdateRequest(
                description = "없는 지출 수정",
                amount = BigDecimal("1000.00"),
                category = ExpenseCategory.MEAL
            )

            // when & then
            val exception = assertThrows<CustomLogicException> {
                expenseService.updateExpense(999L, request)
            }
            assertThat(exception.exceptionCode).isEqualTo(ExceptionCode.EXPENSE_NOT_FOUND)
        }

        @Test
        @DisplayName("예산 부족으로 수정 실패")
        fun insufficientBalance_throwsException() {
            // given
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
            whenever(expenseRepository.findWithTeamAndBudgetById(1L)).thenReturn(original)

            // when & then
            val exception = assertThrows<CustomLogicException> {
                expenseService.updateExpense(1L, request)
            }
            assertThat(exception.exceptionCode).isEqualTo(ExceptionCode.INSUFFICIENT_BALANCE)
        }
    }

    @Nested
    @DisplayName("지출 삭제 테스트")
    internal inner class DeleteExpenseTests {

        @Test
        @DisplayName("지출 삭제 성공")
        fun success() {
            // given
            val expense = Expense(
                id = 1L,
                description = "삭제할 지출",
                amount = BigDecimal("30000.00"),
                category = ExpenseCategory.MEAL,
                paymentMethod = PaymentMethod.CASH,
                payer = payer,
                team = team
            )
            whenever(expenseRepository.findWithTeamAndBudgetById(1L)).thenReturn(expense)

            // when
            val response = expenseService.deleteExpense(1L)

            // then
            val expectedBalance = BigDecimal("130000.00")
            assertThat(team.budget?.balance).isEqualByComparingTo(expectedBalance)
            assertThat(response.balance).isEqualByComparingTo(expectedBalance)

            verify(expenseRepository).delete(expense)
        }

        @Test
        @DisplayName("존재하지 않는 지출 삭제 시 예외 발생")
        fun expenseNotFound_throwsException() {
            // given
            whenever(expenseRepository.findWithTeamAndBudgetById(999L)).thenReturn(null)

            // when & then
            val exception = assertThrows<CustomLogicException> {
                expenseService.deleteExpense(999L)
            }
            assertThat(exception.exceptionCode).isEqualTo(ExceptionCode.EXPENSE_NOT_FOUND)
        }
    }

    @Nested
    @DisplayName("지출 리스트 조회 테스트")
    internal inner class GetListExpenseTests {

        @Test
        @DisplayName("지출 리스트 정상 반환")
        fun success() {
            // given
            val pageable: Pageable = PageRequest.of(0, 10)

            val expenses = listOf(
                createExpense("점심 식사", BigDecimal("10000.00"), PaymentMethod.CASH),
                createExpense("저녁 식사", BigDecimal("15000.00"), PaymentMethod.CARD)
            ).map { ExpenseMapper.toExpenseResponse(it) }

            val page = PageImpl(expenses, pageable, expenses.size.toLong())

            whenever(teamRepository.existsById(1L)).thenReturn(true)
            whenever(expenseRepository.findResponsesByTeamId(1L, pageable)).thenReturn(page)

            // when
            val result = expenseService.getExpenses(1L, pageable)

            // then
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
            // given
            val pageable = PageRequest.of(0, 5)
            whenever(teamRepository.existsById(999L)).thenReturn(false)

            // when & then
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
