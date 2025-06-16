package com.luckyseven.backend.domain.budget.service

import com.luckyseven.backend.domain.budget.dao.BudgetRepository
import com.luckyseven.backend.domain.budget.mapper.BudgetMapper
import com.luckyseven.backend.domain.budget.util.TestUtils
import com.luckyseven.backend.domain.budget.validator.BudgetValidator
import com.luckyseven.backend.domain.expense.repository.ExpenseRepository
import com.luckyseven.backend.domain.team.repository.TeamRepository
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import java.util.*

class BudgetServiceTests {

    val budgetRepository = mockk<BudgetRepository>()
    val teamRepository = mockk<TeamRepository>()
    val expenseRepository = mockk<ExpenseRepository>()
    val budgetMapper = mockk<BudgetMapper>()
    val budgetValidator = mockk<BudgetValidator>()

    val budgetService = BudgetService(
        teamRepository,
        expenseRepository,
        budgetRepository,
        budgetMapper,
        budgetValidator
    )


    @Test
    fun `save는 budgetRepository에 예산을 저장하고 BudgetCreateResponse를 반환한다`() {
        val req = TestUtils.genBudgetCreateReq()
        val leader = TestUtils.genMember()

        val team = TestUtils.genTeam()
        val budget = TestUtils.genBudget()
        val expectedResp = TestUtils.genBudgetCreateResp()

        every { teamRepository.findById(team.id) } returns Optional.of(team)
        every { budgetMapper.toEntity(team, leader.id!!, req) } returns budget
        every { budgetMapper.toCreateResponse(budget) } returns expectedResp
        every { budgetValidator.validateBudgetNotExist(team.id!!) } returns Unit
        every { budgetRepository.save(budget) } returns budget

        val actualResp = budgetService.save(team.id!!, leader.id!!, req)

        actualResp.balance shouldBe expectedResp.balance
        actualResp.avgExchangeRate shouldBe expectedResp.avgExchangeRate
        actualResp.foreignBalance shouldBe expectedResp.foreignBalance

        verify(exactly = 1) { budgetValidator.validateBudgetNotExist(team.id!!) }
        verify(exactly = 1) { budgetRepository.save(budget) }

    }

    @Test
    fun `getByTeamId는 teamId로 예산을 조회하고 BudgetReadResponse를 반환한다`() {
        val teamId = 1L
        val budget = TestUtils.genBudget()
        val expectedResp = TestUtils.genBudgetReadResp()

        every { budgetValidator.validateBudgetExist(teamId) } returns budget
        every { budgetMapper.toReadResponse(budget) } returns expectedResp

        val actualResp = budgetService.getByTeamId(teamId)

        actualResp.totalAmount shouldBe expectedResp.totalAmount
        actualResp.balance shouldBe expectedResp.balance
        actualResp.foreignCurrency shouldBe expectedResp.foreignCurrency

    }

    @Test
    fun `updateByTeamId는 teamId로 예산을 수정하고 BudgetUpdateResponse를 반환한다`() {
        val teamId = 1L
        val loginMemberId = 1L
        val req = TestUtils.genBudgetUpdateReq()

        val budget = TestUtils.genBudget()
        val expectedResp = TestUtils.genBudgetUpdateResp()

        every { budgetValidator.validateBudgetExist(teamId) } returns budget
        every { budgetMapper.toUpdateResponse(budget) } returns expectedResp

        val actualResp = budgetService.updateByTeamId(teamId, loginMemberId, req)

        // 예산이 수정되었는지 확인
        actualResp.balance shouldBe req.totalAmount
        actualResp.balance shouldBe expectedResp.balance
        actualResp.foreignCurrency shouldBe expectedResp.foreignCurrency

    }

    @Test
    fun `addBudgetByTeamId는 teamId로 예산을 추가하고 BudgetUpdateResponse를 반환한다`() {
        val teamId = 1L
        val loginMemberId = 1L
        val req = TestUtils.genBudgetAddReq()

        val budget = TestUtils.genBudget()
        val expectedResp = TestUtils.genBudgetUpdateRespAfterAdd()

        every { budgetValidator.validateBudgetExist(teamId) } returns budget
        every { budgetMapper.toUpdateResponse(budget) } returns expectedResp

        val actualResp = budgetService.addBudgetByTeamId(teamId, loginMemberId, req)

        budget.totalAmount shouldBe expectedResp.balance
        actualResp.balance shouldBe expectedResp.balance
        actualResp.foreignCurrency shouldBe expectedResp.foreignCurrency
    }

    @Test
    fun `deleteByTeamId는 teamId로 예산을 삭제한다`() {
        val teamId = 1L
        val team = TestUtils.genTeam()
        val budget = TestUtils.genBudget()

        every { teamRepository.findById(teamId) } returns Optional.of(team)
        every { budgetValidator.validateBudgetExist(teamId) } returns budget
        every { expenseRepository.existsByTeamId(teamId) } returns false
        every { teamRepository.save(team) } returns team
        every { budgetRepository.delete(budget) } returns Unit

        budgetService.deleteByTeamId(teamId)

        verify(exactly = 1) { expenseRepository.existsByTeamId(teamId) }
        verify(exactly = 1) { teamRepository.save(team) }
        verify(exactly = 1) { budgetRepository.delete(budget) }
    }
}