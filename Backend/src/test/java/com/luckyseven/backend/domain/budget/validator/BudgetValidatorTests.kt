package com.luckyseven.backend.domain.budget.validator

import com.luckyseven.backend.domain.budget.dao.BudgetRepository
import com.luckyseven.backend.domain.budget.dto.BudgetAddRequest
import com.luckyseven.backend.domain.budget.dto.BudgetCreateRequest
import com.luckyseven.backend.domain.budget.dto.BudgetUpdateRequest
import com.luckyseven.backend.domain.budget.entity.Budget
import com.luckyseven.backend.domain.budget.entity.CurrencyCode
import com.luckyseven.backend.domain.budget.util.TestUtils.genBudget
import com.luckyseven.backend.domain.budget.util.TestUtils.genBudgetAddReq
import com.luckyseven.backend.domain.budget.util.TestUtils.genBudgetCreateReq
import com.luckyseven.backend.domain.budget.util.TestUtils.genBudgetUpdateReq
import com.luckyseven.backend.sharedkernel.exception.CustomLogicException
import org.assertj.core.api.Assertions
import org.assertj.core.api.ThrowableAssert
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import java.math.BigDecimal
import java.util.*

@ExtendWith(MockitoExtension::class)
internal class BudgetValidatorTests {
    @InjectMocks
    private val budgetValidator: BudgetValidator? = null

    @Mock
    private val budgetRepository: BudgetRepository? = null

    @Test
    @DisplayName("validateBudgetNotExist는 teamId에 해당하는 예산이 존재하지 않다는 것을 확인한다")
    @Throws(Exception::class)
    fun validateBudgetNotExist_verify_budget_not_exist() {
        val teamId = 1L
        val budgetOptional: Optional<Budget?> = Optional.empty<Budget?>()

        Mockito.`when`<Budget?>(budgetRepository!!.findByTeamId(teamId)).thenReturn(budgetOptional)

        budgetValidator!!.validateBudgetNotExist(teamId)

        Assertions.assertThat(budgetOptional.isPresent()).isFalse()
    }

    @Test
    @DisplayName("validateBudgetNotExist는 teamId에 해당하는 예산이 존재하면 409 CONFLICT가 발생한다")
    @Throws(Exception::class)
    fun validateBudgetNotExist_should_return_409() {
        val teamId = 1L
        val budget = genBudget()

        Mockito.`when`<Budget?>(budgetRepository!!.findByTeamId(teamId)).thenReturn(Optional.of<T?>(budget))

        Assertions.assertThatThrownBy(ThrowableAssert.ThrowingCallable { budgetValidator!!.validateBudgetNotExist(teamId) })
            .isInstanceOf(CustomLogicException::class.java)
            .hasMessageContaining("팀에 이미 존재하는 예산이 있습니다.")
    }

    @Test
    @DisplayName("validateBudgetExist는 teamId에 해당하는 예산이 존재하면 Budget을 반환한다")
    @Throws(Exception::class)
    fun validateBudgetExist_should_return_Budget() {
        val teamId = 1L
        val expectedBudget = genBudget()

        Mockito.`when`<Budget?>(budgetRepository!!.findByTeamId(teamId)).thenReturn(Optional.of<T?>(expectedBudget))

        val actualBudget = budgetValidator!!.validateBudgetExist(teamId)

        Assertions.assertThat(actualBudget.totalAmount).isEqualTo(expectedBudget.totalAmount)
        Assertions.assertThat(actualBudget.balance).isEqualTo(expectedBudget.balance)
        Assertions.assertThat<CurrencyCode?>(actualBudget.foreignCurrency).isEqualTo(expectedBudget.foreignCurrency)
    }

    @Test
    @DisplayName("validateBudgetExist는 teamId에 해당하는 예산이 존재하지 않으면 404 NOT FOUND가 발생한다")
    @Throws(Exception::class)
    fun validateBudgetExist_should_return_404() {
        val teamId = 1L

        Assertions.assertThatThrownBy(ThrowableAssert.ThrowingCallable { budgetValidator!!.validateBudgetExist(teamId) })
            .isInstanceOf(CustomLogicException::class.java)
            .hasMessageContaining("팀을 찾을 수 없습니다.")
    }

    @Test
    @DisplayName("validateRequest는 유효한 BudgetCreateRequest인지 검증한다")
    @Throws(Exception::class)
    fun validateRequest_verify_BudgetCreateRequest() {
        val req = genBudgetCreateReq()

        Assertions.assertThatCode(ThrowableAssert.ThrowingCallable { budgetValidator!!.validateRequest(req) })
            .doesNotThrowAnyException()
    }

    @Test
    @DisplayName("validateRequest는 환전 여부가 true인데 환율이 없으면 400 BAD REQUEST가 발생한다")
    @Throws(Exception::class)
    fun validateRequest_with_CreateReq_should_return_400_1() {
        val req = BudgetCreateRequest(
            BigDecimal.valueOf(100000),
            true,
            null,
            CurrencyCode.USD
        )

        Assertions.assertThatThrownBy(ThrowableAssert.ThrowingCallable { budgetValidator!!.validateRequest(req) })
            .isInstanceOf(CustomLogicException::class.java)
            .hasMessageContaining("잘못된 요청입니다.")
    }

    @Test
    @DisplayName("validateRequest는 환전 여부가 false인데 환율이 있으면 400 BAD REQUEST가 발생한다")
    @Throws(Exception::class)
    fun validateRequest_with_CreateReq_should_return_400_2() {
        val req = BudgetCreateRequest(
            BigDecimal.valueOf(100000),
            false,
            BigDecimal.valueOf(1393.7),
            CurrencyCode.USD
        )

        Assertions.assertThatThrownBy(ThrowableAssert.ThrowingCallable { budgetValidator!!.validateRequest(req) })
            .isInstanceOf(CustomLogicException::class.java)
            .hasMessageContaining("잘못된 요청입니다.")
    }

    @Test
    @DisplayName("validateRequest는 유효한 BudgetUpdateRequest 검증한다")
    @Throws(Exception::class)
    fun validateRequest_verify_BudgetUpdateRequest() {
        val req = genBudgetUpdateReq()

        Assertions.assertThatCode(ThrowableAssert.ThrowingCallable { budgetValidator!!.validateRequest(req) })
            .doesNotThrowAnyException()
    }

    @Test
    @DisplayName("validateRequest는 환전 여부가 true인데 환율이 없으면 400 BAD REQUEST가 발생한다")
    @Throws(Exception::class)
    fun validateRequest_with_UpdateReq_should_return_400_1() {
        val req = BudgetUpdateRequest(
            BigDecimal.valueOf(80000),
            true,
            null
        )

        Assertions.assertThatThrownBy(ThrowableAssert.ThrowingCallable { budgetValidator!!.validateRequest(req) })
            .isInstanceOf(CustomLogicException::class.java)
            .hasMessageContaining("잘못된 요청입니다.")
    }

    @Test
    @DisplayName("validateRequest는 환전 여부가 false인데 환율이 있으면 400 BAD REQUEST가 발생한다")
    @Throws(Exception::class)
    fun validateRequest_with_UpdateReq_should_return_400_2() {
        val req = BudgetUpdateRequest(
            BigDecimal.valueOf(80000),
            false,
            BigDecimal.valueOf(1393.7)
        )

        Assertions.assertThatThrownBy(ThrowableAssert.ThrowingCallable { budgetValidator!!.validateRequest(req) })
            .isInstanceOf(CustomLogicException::class.java)
            .hasMessageContaining("잘못된 요청입니다.")
    }

    @Test
    @DisplayName("validateRequest는 유효한 BudgetAddRequest 검증한다")
    @Throws(Exception::class)
    fun validateRequest_verify_BudgetAddRequest() {
        val req = genBudgetAddReq()

        Assertions.assertThatCode(ThrowableAssert.ThrowingCallable { budgetValidator!!.validateRequest(req) })
            .doesNotThrowAnyException()
    }

    @Test
    @DisplayName("validateRequest는 환전 여부가 true인데 환율이 없으면 400 BAD REQUEST가 발생한다")
    @Throws(Exception::class)
    fun validateRequest_with_AddReq_should_return_400_1() {
        val req = BudgetAddRequest(
            BigDecimal.valueOf(50000),
            true,
            null
        )

        Assertions.assertThatThrownBy(ThrowableAssert.ThrowingCallable { budgetValidator!!.validateRequest(req) })
            .isInstanceOf(CustomLogicException::class.java)
            .hasMessageContaining("잘못된 요청입니다.")
    }

    @Test
    @DisplayName("validateRequest는 환전 여부가 false인데 환율이 있으면 400 BAD REQUEST가 발생한다")
    @Throws(Exception::class)
    fun validateRequest_with_AddReq_should_return_400_2() {
        val req = BudgetAddRequest(
            BigDecimal.valueOf(50000),
            false,
            BigDecimal.valueOf(1393.7)
        )

        Assertions.assertThatThrownBy(ThrowableAssert.ThrowingCallable { budgetValidator!!.validateRequest(req) })
            .isInstanceOf(CustomLogicException::class.java)
            .hasMessageContaining("잘못된 요청입니다.")
    }
}