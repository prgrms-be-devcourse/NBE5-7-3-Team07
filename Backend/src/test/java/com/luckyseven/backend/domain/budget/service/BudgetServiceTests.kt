package com.luckyseven.backend.domain.budget.service

import com.luckyseven.backend.domain.budget.dao.BudgetRepository
import com.luckyseven.backend.domain.budget.dto.BudgetCreateResponse
import com.luckyseven.backend.domain.budget.dto.BudgetReadResponse
import com.luckyseven.backend.domain.budget.dto.BudgetUpdateResponse
import com.luckyseven.backend.domain.budget.entity.Budget
import com.luckyseven.backend.domain.budget.entity.CurrencyCode
import com.luckyseven.backend.domain.budget.mapper.BudgetMapper
import com.luckyseven.backend.domain.budget.util.TestUtils
import com.luckyseven.backend.domain.budget.validator.BudgetValidator
import com.luckyseven.backend.domain.expense.repository.ExpenseRepository
import com.luckyseven.backend.domain.team.entity.Team
import com.luckyseven.backend.domain.team.repository.TeamRepository
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import java.util.*

@ExtendWith(MockitoExtension::class)
internal class BudgetServiceTests {
    @InjectMocks
    private lateinit var budgetService: BudgetService

    @Mock
    private lateinit var budgetRepository: BudgetRepository

    @Mock
    private lateinit var teamRepository: TeamRepository

    @Mock
    private lateinit var expenseRepository: ExpenseRepository

    @Mock
    private lateinit var budgetMapper: BudgetMapper

    @Mock
    private lateinit var budgetValidator: BudgetValidator

    @Test
    @DisplayName("save는 budgetRepository에 예산을 저장하고 BudgetCreateResponse를 반환한다")
    @Throws(Exception::class)
    fun save_should_return_BudgetCreateResponse() {
        val req = TestUtils.genBudgetCreateReq()
        val leader = TestUtils.genMember()

        val team = TestUtils.genTeam()
        val budget = TestUtils.genBudget()
        val expectedResp = TestUtils.genBudgetCreateResp()

        Mockito.`when`<Optional<Team?>?>(teamRepository!!.findById(team.id)).thenReturn(Optional.of<Team?>(team))
        Mockito.`when`<Budget?>(budgetMapper!!.toEntity(team, leader.id!!, req)).thenReturn(budget)
        Mockito.`when`<BudgetCreateResponse?>(budgetMapper.toCreateResponse(budget)).thenReturn(expectedResp)

        val actualResp = budgetService!!.save(team.id!!, leader.id!!, req)

        Assertions.assertThat(actualResp.balance).isEqualTo(expectedResp.balance)
        Assertions.assertThat(actualResp.avgExchangeRate).isEqualTo(expectedResp.avgExchangeRate)
        Assertions.assertThat(actualResp.foreignBalance).isEqualTo(expectedResp.foreignBalance)
        Mockito.verify<BudgetValidator?>(budgetValidator, Mockito.times(1)).validateBudgetNotExist(team.id!!)
        Mockito.verify<BudgetRepository?>(budgetRepository, Mockito.times(1)).save<Budget?>(budget)
    }

    @Test
    @DisplayName("getByTeamId는 teamId로 예산을 조회하고 BudgetReadResponse를 반환한다")
    @Throws(Exception::class)
    fun getByTeamId_should_return_BudgetReadResponse() {
        val teamId = 1L
        val budget = TestUtils.genBudget()
        val expectedResp = TestUtils.genBudgetReadResp()

        Mockito.`when`<Budget?>(budgetValidator!!.validateBudgetExist(teamId)).thenReturn(budget)
        Mockito.`when`<BudgetReadResponse?>(budgetMapper!!.toReadResponse(budget)).thenReturn(expectedResp)

        val actualResp = budgetService!!.getByTeamId(teamId)

        Assertions.assertThat(actualResp.totalAmount).isEqualTo(expectedResp.totalAmount)
        Assertions.assertThat(actualResp.balance).isEqualTo(expectedResp.balance)
        Assertions.assertThat<CurrencyCode?>(actualResp.foreignCurrency).isEqualTo(expectedResp.foreignCurrency)
    }

    @Test
    @DisplayName("updateByTeamId는 teamId로 예산을 수정하고 BudgetUpdateResponse를 반환한다")
    @Throws(Exception::class)
    fun updateByTeamId_should_return_BudgetUpdateResponse() {
        val teamId = 1L
        val loginMemberId = 1L
        val req = TestUtils.genBudgetUpdateReq()

        val budget = TestUtils.genBudget()
        val expectedResp = TestUtils.genBudgetUpdateResp()

        Mockito.`when`<Budget?>(budgetValidator!!.validateBudgetExist(teamId)).thenReturn(budget)
        Mockito.`when`<BudgetUpdateResponse?>(budgetMapper!!.toUpdateResponse(budget)).thenReturn(expectedResp)

        val actualResp = budgetService!!.updateByTeamId(teamId, loginMemberId, req)

        // 예산이 수정되었는지 확인
        Assertions.assertThat(budget.totalAmount).isEqualTo(req.totalAmount)
        Assertions.assertThat(actualResp.balance).isEqualTo(expectedResp.balance)
        Assertions.assertThat<CurrencyCode?>(actualResp.foreignCurrency).isEqualTo(expectedResp.foreignCurrency)
    }

    @Test
    @DisplayName("addBudgetByTeamId는 teamId로 예산을 추가하고 BudgetUpdateResponse를 반환한다")
    @Throws(Exception::class)
    fun addBudgetByTeamId_should_return_BudgetUpdateResponse() {
        val teamId = 1L
        val loginMemberId = 1L
        val req = TestUtils.genBudgetAddReq()

        val budget = TestUtils.genBudget()
        val expectedResp = TestUtils.genBudgetUpdateRespAfterAdd()

        Mockito.`when`<Budget?>(budgetValidator!!.validateBudgetExist(teamId)).thenReturn(budget)
        Mockito.`when`<BudgetUpdateResponse?>(budgetMapper!!.toUpdateResponse(budget)).thenReturn(expectedResp)

        val actualResp = budgetService!!.addBudgetByTeamId(teamId, loginMemberId, req)

        Assertions.assertThat(budget.totalAmount).isEqualTo(expectedResp.balance)
        Assertions.assertThat(actualResp.balance).isEqualTo(expectedResp.balance)
        Assertions.assertThat<CurrencyCode?>(actualResp.foreignCurrency).isEqualTo(expectedResp.foreignCurrency)
    }

    @Test
    @DisplayName("deleteByTeamId는 teamId로 예산을 삭제한다")
    @Throws(Exception::class)
    fun deleteByTeamId_should_delete_Budget_by_teamId() {
        val teamId = 1L
        val team = TestUtils.genTeam()
        val budget = TestUtils.genBudget()

        Mockito.`when`<Optional<Team?>?>(teamRepository!!.findById(teamId)).thenReturn(Optional.of<Team?>(team))
        Mockito.`when`<Budget?>(budgetValidator!!.validateBudgetExist(teamId)).thenReturn(budget)
        Mockito.`when`<Boolean?>(expenseRepository!!.existsByTeamId(teamId)).thenReturn(false)

        budgetService!!.deleteByTeamId(teamId)

        Mockito.verify<ExpenseRepository?>(expenseRepository, Mockito.times(1)).existsByTeamId(teamId)
        Mockito.verify<TeamRepository?>(teamRepository, Mockito.times(1)).save<Team?>(team)
        Mockito.verify<BudgetRepository?>(budgetRepository, Mockito.times(1)).delete(budget)
    }
}