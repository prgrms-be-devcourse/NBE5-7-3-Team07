package com.luckyseven.backend.domain.expense.service

import com.luckyseven.backend.domain.budget.entity.Budget
import com.luckyseven.backend.domain.budget.entity.CurrencyCode
import com.luckyseven.backend.domain.expense.cache.CacheEvictService
import com.luckyseven.backend.domain.expense.dto.ExpenseRequest
import com.luckyseven.backend.domain.expense.dto.ExpenseResponse
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
import com.luckyseven.backend.sharedkernel.dto.PageResponse
import com.luckyseven.backend.sharedkernel.exception.CustomLogicException
import com.luckyseven.backend.sharedkernel.exception.ExceptionCode
import org.assertj.core.api.Assertions
import org.assertj.core.api.ThrowableAssert
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import java.math.BigDecimal
import java.util.*

@ExtendWith(MockitoExtension::class)
internal class ExpenseServiceTest {
    @Mock
    private val teamRepository: TeamRepository? = null

    @Mock
    private val memberService: MemberService? = null

    @Mock
    private val expenseRepository: ExpenseRepository? = null

    @Mock
    private val settlementService: SettlementService? = null

    @Mock
    private val cacheEvictService: CacheEvictService? = null

    @InjectMocks
    private val expenseService: ExpenseService? = null

    private var team: Team? = null
    private var budget: Budget? = null
    private var payer: Member? = null
    private var settler1: Member? = null
    private var settler2: Member? = null

    @BeforeEach
    fun setUp() {
        payer = Member(
            id = 1L,
            email = "dldldldl@naver.com",
            password = "1234",
            nickname = "하하하하하"
        )
        settler1 = Member(
            id = 10L,
            email = "settler1@example.com",
            password = "abcd1234",
            nickname = "정산자1"
        )
        settler2 = Member(
            id = 20L,
            email = "settler2@example.com",
            password = "qwer5678",
            nickname = "정산자2"
        )
        budget = Budget(
            id = null,
            team = null,
            totalAmount = BigDecimal("100000.00"),
            setBy = payer!!.id!!,
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
            leader = payer!!,
            budget = budget
        )
        budget!!.setTeam(team)
    }

    @Nested
    @DisplayName("지출 등록 테스트")
    internal inner class SaveExpenseTests {
        @Test
        @DisplayName("지출 등록 성공")
        fun success() {
            val request = ExpenseRequest(
                description = "럭키비키즈 점심 식사",
                amount = BigDecimal("10000.00"),
                category = ExpenseCategory.MEAL,
                payerId = 1L,
                paymentMethod = PaymentMethod.CASH,
                settlerId = mutableListOf(10L, 20L)
            )

            Mockito.`when`(expenseRepository!!.save(ArgumentMatchers.any(Expense::class.java)))
                .thenAnswer { invocation -> invocation.getArgument<Expense>(0) }
            Mockito.`when`(teamRepository!!.findTeamWithBudget(1L))
                .thenReturn(Optional.of(team!!))
            Mockito.`when`(memberService!!.findMemberOrThrow(1L)).thenReturn(payer)

            val response = expenseService!!.saveExpense(1L, request)

            val captor = ArgumentCaptor.forClass(Expense::class.java)
            Mockito.verify(expenseRepository).save(captor.capture())
            val saved = captor.value
            Assertions.assertThat(saved.description).isEqualTo(request.description)

            val expectedBalance = BigDecimal("100000.00").subtract(request.amount)
            Assertions.assertThat(budget!!.balance).isEqualByComparingTo(expectedBalance)
            Assertions.assertThat(response.balance).isEqualByComparingTo(expectedBalance)
        }

        @Nested
        internal inner class ExceptionCases {
            @Test
            @DisplayName("존재하지 않는 팀")
            fun teamNotFound_throwsException() {
                Mockito.`when`(teamRepository!!.findTeamWithBudget(999L))
                    .thenReturn(Optional.empty())
                val request = ExpenseRequest(
                    description = "변종된 럭키비키즈 나쁜 점심 식사",
                    amount = BigDecimal("1000.00"),
                    category = ExpenseCategory.MEAL,
                    payerId = 1L,
                    paymentMethod = PaymentMethod.CASH,
                    settlerId = mutableListOf(10L, 20L)
                )

                Assertions.assertThatThrownBy(ThrowableAssert.ThrowingCallable {
                    expenseService!!.saveExpense(999L, request)
                })
                    .isInstanceOf(CustomLogicException::class.java)
                    .extracting("exceptionCode")
                    .isEqualTo(ExceptionCode.TEAM_NOT_FOUND)
            }

            @Test
            @DisplayName("예산보다 큰 지출 금액")
            fun insufficientBalance_throwsException() {
                Mockito.`when`(teamRepository!!.findTeamWithBudget(1L))
                    .thenReturn(Optional.of(team!!))
                val request = ExpenseRequest(
                    description = "럭키비키즈 팀 배부르게 식사",
                    amount = BigDecimal("1000000.00"),
                    category = ExpenseCategory.MEAL,
                    payerId = 1L,
                    paymentMethod = PaymentMethod.CARD,
                    settlerId = mutableListOf(10L)
                )

                Assertions.assertThatThrownBy(ThrowableAssert.ThrowingCallable {
                    expenseService!!.saveExpense(1L, request)
                })
                    .isInstanceOf(CustomLogicException::class.java)
                    .extracting("exceptionCode")
                    .isEqualTo(ExceptionCode.INSUFFICIENT_BALANCE)
            }
        }

        @Test
        @DisplayName("지출 등록 성공 및 정산 생성 호출")
        fun success_and_createSettlements() {
            val request = ExpenseRequest(
                description = "럭키비키즈 점심 식사",
                amount = BigDecimal("50000.00"),
                category = ExpenseCategory.MEAL,
                payerId = 1L,
                paymentMethod = PaymentMethod.CASH,
                settlerId = mutableListOf(10L, 20L)
            )

            Mockito.`when`(expenseRepository!!.save(ArgumentMatchers.any(Expense::class.java)))
                .thenAnswer { invocation -> invocation.getArgument<Expense>(0) }
            Mockito.`when`(teamRepository!!.findTeamWithBudget(1L))
                .thenReturn(Optional.of(team!!))
            Mockito.`when`(memberService!!.findMemberOrThrow(1L))
                .thenReturn(payer)

            val response = expenseService!!.saveExpense(1L, request)

            val expenseCaptor = ArgumentCaptor.forClass(Expense::class.java)
            Mockito.verify(expenseRepository).save(expenseCaptor.capture())
            val savedExpense = expenseCaptor.value
            Assertions.assertThat(savedExpense.description).isEqualTo(request.description)

            val expectedBalance = BigDecimal("100000.00").subtract(request.amount)
            Assertions.assertThat(budget!!.balance).isEqualByComparingTo(expectedBalance)
            Assertions.assertThat(response.balance).isEqualByComparingTo(expectedBalance)


            Mockito.verify(settlementService!!).createAllSettlements(
                ArgumentMatchers.eq(request),
                ArgumentMatchers.eq(payer!!),
                ArgumentMatchers.eq(savedExpense)
            )
        }
    }

    @Nested
    @DisplayName("지출 조회 테스트")
    internal inner class GetExpenseTests {
        @Test
        @DisplayName("지출 조회 성공")
        fun success() {
            val expense = Expense(
                id = 1L,
                description = "럭키비키즈 점심 식사",
                amount = BigDecimal("50000.00"),
                category = ExpenseCategory.MEAL,
                paymentMethod = PaymentMethod.CARD,
                payer = payer!!,
                team = team!!
            )
            Mockito.`when`(expenseRepository!!.findByIdWithPayer(expense.id!!))
                .thenReturn(expense)

            val response = expenseService!!.getExpense(expense.id!!)

            Assertions.assertThat(response.description).isEqualTo(expense.description)
            Assertions.assertThat(response.amount).isEqualByComparingTo(expense.amount)
            Assertions.assertThat(response.category).isEqualTo(expense.category)
            Assertions.assertThat(response.paymentMethod).isEqualTo(expense.paymentMethod)
            Mockito.verify(expenseRepository).findByIdWithPayer(expense.id!!)
        }

        @Test
        @DisplayName("존재하지 않는 지출 조회 시 예외 발생")
        fun notFound_throwsException() {
            val expenseId = 999L
            Mockito.`when`(expenseRepository!!.findByIdWithPayer(expenseId)).thenReturn(null)

            Assertions.assertThatThrownBy(ThrowableAssert.ThrowingCallable {
                expenseService!!.getExpense(expenseId)
            })
                .isInstanceOf(CustomLogicException::class.java)
                .extracting("exceptionCode")
                .isEqualTo(ExceptionCode.EXPENSE_NOT_FOUND)
        }
    }

    @Nested
    @DisplayName("지출 수정 테스트")
    internal inner class UpdateExpenseTests {
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
                paymentMethod = PaymentMethod.CASH, // 실제 paymentMethod 필요
                payer = payer!!,
                team = team!!
            )
            Mockito.`when`(expenseRepository!!.findWithTeamAndBudgetById(1L)).thenReturn(original)

            val response = expenseService!!.updateExpense(1L, request)

            Assertions.assertThat(original.description).isEqualTo(request.description)
            Assertions.assertThat(original.amount).isEqualByComparingTo(request.amount)
            val expectedDelta = request.amount!!.subtract(BigDecimal("50000.00"))
            val expectedBalance = BigDecimal("100000.00").subtract(expectedDelta)
            Assertions.assertThat(budget!!.balance).isEqualByComparingTo(expectedBalance)
            Assertions.assertThat(response.balance).isEqualByComparingTo(expectedBalance)
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
                payer = payer!!,
                team = team!!
            )
            Mockito.`when`(expenseRepository!!.findWithTeamAndBudgetById(1L)).thenReturn(original)

            val response = expenseService!!.updateExpense(1L, request)

            val expectedDelta = request.amount!!.subtract(BigDecimal("50000.00"))
            val expectedBalance = BigDecimal("100000.00").subtract(expectedDelta)
            Assertions.assertThat(budget!!.balance).isEqualByComparingTo(expectedBalance)
            Assertions.assertThat(response.balance).isEqualByComparingTo(expectedBalance)
        }

        @Test
        @DisplayName("존재하지 않는 지출")
        fun expenseNotFound_throwsException() {
            Mockito.`when`(expenseRepository!!.findWithTeamAndBudgetById(999L)).thenReturn(null)
            val request = ExpenseUpdateRequest(
                description = "없는 지출 수정",
                amount = BigDecimal("1000.00"),
                category = ExpenseCategory.MEAL
            )

            Assertions.assertThatThrownBy(ThrowableAssert.ThrowingCallable {
                expenseService!!.updateExpense(999L, request)
            })
                .isInstanceOf(CustomLogicException::class.java)
                .extracting("exceptionCode")
                .isEqualTo(ExceptionCode.EXPENSE_NOT_FOUND)
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
                payer = payer!!,
                team = team!!
            )
            Mockito.`when`(expenseRepository!!.findWithTeamAndBudgetById(1L)).thenReturn(original)

            Assertions.assertThatThrownBy(ThrowableAssert.ThrowingCallable {
                expenseService!!.updateExpense(1L, request)
            })
                .isInstanceOf(CustomLogicException::class.java)
                .extracting("exceptionCode")
                .isEqualTo(ExceptionCode.INSUFFICIENT_BALANCE)
        }
    }

    @Nested
    @DisplayName("지출 삭제 테스트")
    internal inner class DeleteExpenseTests {
        @Test
        @DisplayName("지출 삭제 성공")
        fun success() {
            val expense = Expense(
                id = 1L,
                description = "삭제할 지출",
                amount = BigDecimal("30000.00"),
                category = ExpenseCategory.MEAL,
                paymentMethod = PaymentMethod.CASH,
                payer = payer!!,
                team = team!!
            )
            Mockito.`when`(expenseRepository!!.findWithTeamAndBudgetById(1L)).thenReturn(expense)

            val response = expenseService!!.deleteExpense(1L)

            val expectedBalance = BigDecimal("130000.00")
            Assertions.assertThat(team!!.budget!!.balance).isEqualByComparingTo(expectedBalance)
            Assertions.assertThat(response.balance).isEqualByComparingTo(expectedBalance)

            Mockito.verify(expenseRepository).delete(expense)
        }

        @Test
        @DisplayName("존재하지 않는 지출 삭제 시 예외 발생")
        fun expenseNotFound_throwsException() {
            Mockito.`when`(expenseRepository!!.findWithTeamAndBudgetById(999L)).thenReturn(null)

            Assertions.assertThatThrownBy(ThrowableAssert.ThrowingCallable {
                expenseService!!.deleteExpense(999L)
            })
                .isInstanceOf(CustomLogicException::class.java)
                .extracting("exceptionCode")
                .isEqualTo(ExceptionCode.EXPENSE_NOT_FOUND)
        }
    }

    @Nested
    @DisplayName("지출 리스트 조회 테스트")
    internal inner class GetListExpenseTests {
        @Test
        @DisplayName("지출 리스트 정상 반환")
        fun success() {
            val pageable: Pageable = PageRequest.of(0, 10)

            val expenses: List<ExpenseResponse> = listOf(
                Expense(
                    id = null,
                    description = "점심 식사",
                    amount = BigDecimal("10000.00"),
                    category = ExpenseCategory.MEAL,
                    paymentMethod = PaymentMethod.CASH,
                    payer = payer!!,
                    team = team!!
                ).let { ExpenseMapper.toExpenseResponse(it) },
                Expense(
                    id = null,
                    description = "저녁 식사",
                    amount = BigDecimal("15000.00"),
                    category = ExpenseCategory.MEAL,
                    paymentMethod = PaymentMethod.CARD,
                    payer = payer!!,
                    team = team!!
                ).let { ExpenseMapper.toExpenseResponse(it) }
            )
            val page: Page<ExpenseResponse> = PageImpl(expenses, pageable, expenses.size.toLong())

            Mockito.`when`(teamRepository!!.existsById(1L)).thenReturn(true)
            Mockito.`when`(expenseRepository!!.findResponsesByTeamId(1L, pageable)).thenReturn(page)

            val result: PageResponse<ExpenseResponse> = expenseService!!.getExpenses(1L, pageable)

            Assertions.assertThat(result.content).hasSize(2)
            Assertions.assertThat(result.page).isEqualTo(0)
            Assertions.assertThat(result.size).isEqualTo(10)
            Assertions.assertThat(result.totalElements).isEqualTo(2)
            Assertions.assertThat(result.totalPages).isEqualTo(1)

            val first: ExpenseResponse = result.content[0]
            Assertions.assertThat(first.description).isEqualTo("점심 식사")
            Assertions.assertThat(first.amount).isEqualByComparingTo(BigDecimal("10000.00"))
        }

        @Test
        @DisplayName("존재하지 않는 팀으로 조회 시 예외 발생")
        fun teamNotFound() {
            val pageable: Pageable = PageRequest.of(0, 5)
            Mockito.`when`(teamRepository!!.existsById(999L)).thenReturn(false)

            Assertions.assertThatThrownBy(ThrowableAssert.ThrowingCallable {
                expenseService!!.getExpenses(999L, pageable)
            })
                .isInstanceOf(CustomLogicException::class.java)
                .extracting("exceptionCode")
                .isEqualTo(ExceptionCode.TEAM_NOT_FOUND)
        }
    }
}
