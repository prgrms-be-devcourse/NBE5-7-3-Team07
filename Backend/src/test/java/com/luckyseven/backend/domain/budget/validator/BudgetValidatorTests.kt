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
import com.luckyseven.backend.sharedkernel.exception.ExceptionCode
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkClass
import org.assertj.core.api.Assertions
import org.assertj.core.api.ThrowableAssert
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import java.math.BigDecimal
import java.util.*

class BudgetValidatorTests {

    val budgetRepository = mockk<BudgetRepository>()
    val budgetValidator = BudgetValidator(budgetRepository)

    @Test
    fun `validateBudgetNotExist는 teamId에 해당하는 예산이 존재하지 않다는 것을 확인한다`() {
        val teamId = 1L
        val budgetOptional: Budget? = null

        every { budgetRepository.findByTeamId(teamId) } returns budgetOptional

        budgetValidator.validateBudgetNotExist(teamId)

    }

    @Test
    fun `validateBudgetNotExist는 teamId에 해당하는 예산이 존재하면 409 CONFLICT가 발생한다`() {
        val teamId = 1L
        val budget = genBudget()
        val expectedMsg = "팀에 이미 존재하는 예산이 있습니다."

        every { budgetRepository.findByTeamId(teamId) } returns budget

        val exception = assertThrows<CustomLogicException> {
            budgetValidator.validateBudgetNotExist(teamId)
        }

        exception.message shouldBe expectedMsg
        exception.exceptionCode shouldBe ExceptionCode.BUDGET_CONFLICT
    }

    @Test
    fun `validateBudgetExist는 teamId에 해당하는 예산이 존재하면 Budget을 반환한다`() {
        val teamId = 1L
        val expectedBudget = genBudget()

        every { budgetRepository.findByTeamId(teamId) } returns expectedBudget

        val actualBudget = budgetValidator.validateBudgetExist(teamId)

        actualBudget.totalAmount shouldBe expectedBudget.totalAmount
        actualBudget.balance shouldBe expectedBudget.balance
        actualBudget.foreignCurrency shouldBe expectedBudget.foreignCurrency

    }

    @Test
    fun `validateBudgetExist는 teamId에 해당하는 예산이 존재하지 않으면 404 NOT FOUND가 발생한다`() {
        val teamId = 1L
        val expectedMsg = "팀을 찾을 수 없습니다."

        every { budgetRepository.findByTeamId(teamId) } returns null

        val exception = assertThrows<CustomLogicException> {
            budgetValidator.validateBudgetExist(teamId)
        }

        exception.message shouldBe expectedMsg
        exception.exceptionCode shouldBe ExceptionCode.TEAM_NOT_FOUND
    }

    @Test
    fun `validateRequest는 유효한 BudgetCreateRequest인지 검증한다`() {
        val req = genBudgetCreateReq()

        assertDoesNotThrow {
            budgetValidator.validateRequest(req)
        }

    }

    @Test
    fun `validateRequest는 CreateReq 속 환전 여부가 true인데 환율이 없으면 400 BAD REQUEST가 발생한다`() {
        val req = BudgetCreateRequest(
            BigDecimal.valueOf(100000),
            true,
            null,
            CurrencyCode.USD
        )

        val expectedMsg = "잘못된 요청입니다."

        val exception = assertThrows<CustomLogicException> {
            budgetValidator.validateRequest(req)
        }

        exception.message shouldBe expectedMsg
        exception.exceptionCode shouldBe ExceptionCode.BAD_REQUEST
    }

    @Test
    fun `validateRequest는 CreateReq 속 환전 여부가 false인데 환율이 있으면 400 BAD REQUEST가 발생한다`() {
        val req = BudgetCreateRequest(
            BigDecimal.valueOf(100000),
            false,
            BigDecimal.valueOf(1393.7),
            CurrencyCode.USD
        )

        val expectedMsg = "잘못된 요청입니다."

        val exception = assertThrows<CustomLogicException> {
            budgetValidator.validateRequest(req)
        }

        exception.message shouldBe expectedMsg
        exception.exceptionCode shouldBe ExceptionCode.BAD_REQUEST
    }

    @Test
    fun `validateRequest는 유효한 BudgetUpdateRequest 검증한다`() {
        val req = genBudgetUpdateReq()

        assertDoesNotThrow {
            budgetValidator.validateRequest(req)
        }
    }

    @Test
    fun `validateRequest는 UpdateReq 속 환전 여부가 true인데 환율이 없으면 400 BAD REQUEST가 발생한다`() {
        val req = BudgetUpdateRequest(
            BigDecimal.valueOf(80000),
            true,
            null
        )

        val expectedMsg = "잘못된 요청입니다."

        val exception = assertThrows<CustomLogicException> {
            budgetValidator.validateRequest(req)
        }

        exception.message shouldBe expectedMsg
        exception.exceptionCode shouldBe ExceptionCode.BAD_REQUEST
    }

    @Test
    fun `validateRequest는 UpdateReq 속 환전 여부가 false인데 환율이 있으면 400 BAD REQUEST가 발생한다`() {
        val req = BudgetUpdateRequest(
            BigDecimal.valueOf(80000),
            false,
            BigDecimal.valueOf(1393.7)
        )

        val expectedMsg = "잘못된 요청입니다."

        val exception = assertThrows<CustomLogicException> {
            budgetValidator.validateRequest(req)
        }

        exception.message shouldBe expectedMsg
        exception.exceptionCode shouldBe ExceptionCode.BAD_REQUEST
    }

    @Test
    fun `validateRequest는 유효한 BudgetAddRequest 검증한다`() {
        val req = genBudgetAddReq()

        assertDoesNotThrow {
            budgetValidator.validateRequest(req)
        }
    }

    @Test
    fun `validateRequest는 AddReq 속 환전 여부가 true인데 환율이 없으면 400 BAD REQUEST가 발생한다`() {
        val req = BudgetAddRequest(
            BigDecimal.valueOf(50000),
            true,
            null
        )

        val expectedMsg = "잘못된 요청입니다."

        val exception = assertThrows<CustomLogicException> {
            budgetValidator.validateRequest(req)
        }

        exception.message shouldBe expectedMsg
        exception.exceptionCode shouldBe ExceptionCode.BAD_REQUEST
    }

    @Test
    fun `validateRequest는 AddReq 속 환전 여부가 false인데 환율이 있으면 400 BAD REQUEST가 발생한다`() {
        val req = BudgetAddRequest(
            BigDecimal.valueOf(50000),
            false,
            BigDecimal.valueOf(1393.7)
        )

        val expectedMsg = "잘못된 요청입니다."

        val exception = assertThrows<CustomLogicException> {
            budgetValidator.validateRequest(req)
        }

        exception.message shouldBe expectedMsg
        exception.exceptionCode shouldBe ExceptionCode.BAD_REQUEST
    }
}