package com.luckyseven.backend.domain.budget.service;

import static com.luckyseven.backend.domain.budget.util.TestUtils.genBudget;
import static com.luckyseven.backend.domain.budget.util.TestUtils.genBudgetAddReq;
import static com.luckyseven.backend.domain.budget.util.TestUtils.genBudgetCreateReq;
import static com.luckyseven.backend.domain.budget.util.TestUtils.genBudgetCreateResp;
import static com.luckyseven.backend.domain.budget.util.TestUtils.genBudgetReadResp;
import static com.luckyseven.backend.domain.budget.util.TestUtils.genBudgetUpdateReq;
import static com.luckyseven.backend.domain.budget.util.TestUtils.genBudgetUpdateResp;
import static com.luckyseven.backend.domain.budget.util.TestUtils.genBudgetUpdateRespAfterAdd;
import static com.luckyseven.backend.domain.budget.util.TestUtils.genMember;
import static com.luckyseven.backend.domain.budget.util.TestUtils.genTeam;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.luckyseven.backend.domain.budget.dao.BudgetRepository;
import com.luckyseven.backend.domain.budget.dto.BudgetAddRequest;
import com.luckyseven.backend.domain.budget.dto.BudgetCreateRequest;
import com.luckyseven.backend.domain.budget.dto.BudgetCreateResponse;
import com.luckyseven.backend.domain.budget.dto.BudgetReadResponse;
import com.luckyseven.backend.domain.budget.dto.BudgetUpdateRequest;
import com.luckyseven.backend.domain.budget.dto.BudgetUpdateResponse;
import com.luckyseven.backend.domain.budget.entity.Budget;
import com.luckyseven.backend.domain.budget.mapper.BudgetMapper;
import com.luckyseven.backend.domain.budget.validator.BudgetValidator;
import com.luckyseven.backend.domain.expense.repository.ExpenseRepository;
import com.luckyseven.backend.domain.member.entity.Member;
import com.luckyseven.backend.domain.team.entity.Team;
import com.luckyseven.backend.domain.team.repository.TeamRepository;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BudgetServiceTests {

  @InjectMocks
  private BudgetService budgetService;

  @Mock
  private BudgetRepository budgetRepository;

  @Mock
  private TeamRepository teamRepository;

  @Mock
  private ExpenseRepository expenseRepository;

  @Mock
  private BudgetMapper budgetMapper;

  @Mock
  private BudgetValidator budgetValidator;

  @Test
  @DisplayName("save는 budgetRepository에 예산을 저장하고 BudgetCreateResponse를 반환한다")
  void save_should_return_BudgetCreateResponse() throws Exception {

    BudgetCreateRequest req = genBudgetCreateReq();
    Member leader = genMember();

    Team team = genTeam();
    Budget budget = genBudget();
    BudgetCreateResponse expectedResp = genBudgetCreateResp();

    when(teamRepository.findById(team.getId())).thenReturn(Optional.of(team));
    when(budgetMapper.toEntity(team, leader.getId(), req)).thenReturn(budget);
    when(budgetMapper.toCreateResponse(budget)).thenReturn(expectedResp);

    BudgetCreateResponse actualResp = budgetService.save(team.getId(), leader.getId(), req);

    assertThat(actualResp.balance).isEqualTo(expectedResp.balance);
    assertThat(actualResp.avgExchangeRate).isEqualTo(expectedResp.avgExchangeRate);
    assertThat(actualResp.foreignBalance).isEqualTo(expectedResp.foreignBalance);
    verify(budgetValidator, times(1)).validateBudgetNotExist(team.getId());
    verify(budgetRepository, times(1)).save(budget);
  }

  @Test
  @DisplayName("getByTeamId는 teamId로 예산을 조회하고 BudgetReadResponse를 반환한다")
  void getByTeamId_should_return_BudgetReadResponse() throws Exception {

    Long teamId = 1L;
    Budget budget = genBudget();
    BudgetReadResponse expectedResp = genBudgetReadResp();

    when(budgetValidator.validateBudgetExist(teamId)).thenReturn(budget);
    when(budgetMapper.toReadResponse(budget)).thenReturn(expectedResp);

    BudgetReadResponse actualResp = budgetService.getByTeamId(teamId);

    assertThat(actualResp.totalAmount).isEqualTo(expectedResp.totalAmount);
    assertThat(actualResp.balance).isEqualTo(expectedResp.balance);
    assertThat(actualResp.foreignCurrency).isEqualTo(expectedResp.foreignCurrency);

  }

  @Test
  @DisplayName("updateByTeamId는 teamId로 예산을 수정하고 BudgetUpdateResponse를 반환한다")
  void updateByTeamId_should_return_BudgetUpdateResponse() throws Exception {

    Long teamId = 1L;
    Long loginMemberId = 1L;
    BudgetUpdateRequest req = genBudgetUpdateReq();

    Budget budget = genBudget();
    BudgetUpdateResponse expectedResp = genBudgetUpdateResp();

    when(budgetValidator.validateBudgetExist(teamId)).thenReturn(budget);
    when(budgetMapper.toUpdateResponse(budget)).thenReturn(expectedResp);

    BudgetUpdateResponse actualResp = budgetService.updateByTeamId(teamId, loginMemberId, req);

    // 예산이 수정되었는지 확인
    assertThat(budget.getTotalAmount()).isEqualTo(req.totalAmount);
    assertThat(actualResp.balance).isEqualTo(expectedResp.balance);
    assertThat(actualResp.foreignCurrency).isEqualTo(expectedResp.foreignCurrency);

  }

  @Test
  @DisplayName("addBudgetByTeamId는 teamId로 예산을 추가하고 BudgetUpdateResponse를 반환한다")
  void addBudgetByTeamId_should_return_BudgetUpdateResponse() throws Exception {

    Long teamId = 1L;
    Long loginMemberId = 1L;
    BudgetAddRequest req = genBudgetAddReq();

    Budget budget = genBudget();
    BudgetUpdateResponse expectedResp = genBudgetUpdateRespAfterAdd();

    when(budgetValidator.validateBudgetExist(teamId)).thenReturn(budget);
    when(budgetMapper.toUpdateResponse(budget)).thenReturn(expectedResp);

    BudgetUpdateResponse actualResp = budgetService.addBudgetByTeamId(teamId, loginMemberId, req);

    assertThat(budget.getTotalAmount()).isEqualTo(expectedResp.balance);
    assertThat(actualResp.balance).isEqualTo(expectedResp.balance);
    assertThat(actualResp.foreignCurrency).isEqualTo(expectedResp.foreignCurrency);
  }

  @Test
  @DisplayName("deleteByTeamId는 teamId로 예산을 삭제한다")
  void deleteByTeamId_should_delete_Budget_by_teamId() throws Exception {

    Long teamId = 1L;
    Team team = genTeam();
    Budget budget = genBudget();

    when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));
    when(budgetValidator.validateBudgetExist(teamId)).thenReturn(budget);
    when(expenseRepository.existsByTeamId(teamId)).thenReturn(false);

    budgetService.deleteByTeamId(teamId);

    verify(expenseRepository, times(1)).existsByTeamId(teamId);
    verify(teamRepository, times(1)).save(team);
    verify(budgetRepository, times(1)).delete(budget);
  }

}