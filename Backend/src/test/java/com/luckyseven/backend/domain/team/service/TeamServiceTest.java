package com.luckyseven.backend.domain.team.service;

import com.luckyseven.backend.domain.budget.dao.BudgetRepository;
import com.luckyseven.backend.domain.budget.entity.Budget;
import com.luckyseven.backend.domain.expense.entity.Expense;
import com.luckyseven.backend.domain.expense.repository.ExpenseRepository;
import com.luckyseven.backend.domain.member.entity.Member;
import com.luckyseven.backend.domain.member.repository.MemberRepository;
import com.luckyseven.backend.domain.member.service.utill.MemberDetails;
import com.luckyseven.backend.domain.team.dto.TeamCreateRequest;
import com.luckyseven.backend.domain.team.dto.TeamCreateResponse;
import com.luckyseven.backend.domain.team.dto.TeamDashboardResponse;
import com.luckyseven.backend.domain.team.dto.TeamJoinResponse;
import com.luckyseven.backend.domain.team.entity.Team;
import com.luckyseven.backend.domain.team.entity.TeamMember;
import com.luckyseven.backend.domain.team.repository.TeamMemberRepository;
import com.luckyseven.backend.domain.team.repository.TeamRepository;
import com.luckyseven.backend.sharedkernel.exception.CustomLogicException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
public class TeamServiceTest {

  @Mock
  private TeamRepository teamRepository;

  @Mock
  private TeamMemberRepository teamMemberRepository;

  @Mock
  private BudgetRepository budgetRepository;

  @Mock
  private ExpenseRepository expenseRepository;

  @Mock
  private MemberRepository memberRepository; // Added this line

  @InjectMocks
  private TeamService teamService;

  private Team team;
  private Member creator;
  private MemberDetails memberDetails;
  private TeamCreateRequest request;

  @BeforeEach
  void setUp() {
    creator = Member.builder()
        .id(1L)
        .email("text@example.com")
        .nickname("테스터")
        .build();

    memberDetails = new MemberDetails(creator);

    request = TeamCreateRequest.builder()
        .name("test_team")
        .teamPassword("password")
        .build();

    team = Team.builder()
        .id(1L)
        .name("test_team")
        .teamCode("ABCDEF")
        .leader(creator)
        .teamPassword("password")
        .build();

  }

  @Test
  void createTeam() {
    //given
    given(memberRepository.findById(creator.getId())).willReturn(Optional.of(creator));
    given(teamRepository.save(any(Team.class))).willReturn(team);
    given(teamMemberRepository.save(any(TeamMember.class))).willAnswer(invocation -> invocation.getArgument(0)); // Return the saved entity
    given(budgetRepository.save(any(Budget.class))).willAnswer(invocation -> { // Simulate saving budget and setting ID
        Budget b = invocation.getArgument(0);
      // Budget 저장 시 ID가 할당된다면 이를 시뮬레이션
      // 지금은 단순히 같은 인스턴스를 반환하지만,
      // toTeamCreateResponse에서 ID가 필요하면 mock 수정이나 메서드 로직 조정이 필요
        return b;
    });


    TeamCreateResponse expectedResponse = TeamCreateResponse.builder()
        .id(team.getId())
        .name(team.getName())
        .teamCode(team.getTeamCode())
        .leaderId(team.getLeader().getId())
        .build();

    // given(teamMapper.toTeamCreateResponse(any(Team.class))).willReturn(expectedResponse); // Removed: TeamMapper.toTeamCreateResponse is static

    //when
    TeamCreateResponse result = teamService.createTeam(memberDetails, request);

    //then
    assertThat(result).isNotNull();
    assertThat(result.id).isEqualTo(team.getId());
    assertThat(result.name).isEqualTo(request.name);
    assertThat(team.getTeamPassword()).isEqualTo(request.teamPassword);
    assertThat(result.leaderId).isEqualTo(creator.getId());

    //팀 저장
    ArgumentCaptor<Team> teamCaptor = ArgumentCaptor.forClass(Team.class);
    verify(teamRepository).save(teamCaptor.capture());
    assertThat(teamCaptor.getValue().getName()).isEqualTo(request.name);
    assertThat(teamCaptor.getValue().getLeader().getId()).isEqualTo(creator.getId());
    assertThat(teamCaptor.getValue().getTeamPassword()).isEqualTo(request.teamPassword);

    //팀 멤버 저장
    ArgumentCaptor<TeamMember> teamMemberCaptor = ArgumentCaptor.forClass(TeamMember.class);
    verify(teamMemberRepository).save(teamMemberCaptor.capture());
    TeamMember capturedTeamMember = teamMemberCaptor.getValue();
    assertThat(capturedTeamMember.getMember()).isEqualTo(creator);
    assertThat(capturedTeamMember.getTeam()).isEqualTo(team);
  }

  @Test
  void joinTeam() {
    // given

    String teamCode = "ABCDEF";
    String teamPassword = "password";

    given(teamRepository.findByTeamCode(teamCode)).willReturn(Optional.of(team));

    // 새로운 멤버
    Member newMember = Member.builder()
        .id(2L)
        .nickname("newMem")
        .build();
    MemberDetails newMemberDetails = new MemberDetails(newMember);
    // 새로운 TeamMember 객체 생성
    TeamMember teamMember = TeamMember.builder()
        .team(team)
        .member(newMember)
        .build();

    given(teamMemberRepository.save(any(TeamMember.class))).willReturn(teamMember);
    given(memberRepository.findById(newMember.getId())).willReturn(Optional.of(newMember)); // Mock for the new member

    // TeamJoinResponse expectedResponse = TeamJoinResponse.builder()
    //     .id(team.getId())
    //     .teamName(team.getName())
    //     .teamCode(team.getTeamCode())
    //     .leaderId(team.getLeader().getId())
    //     .build();

    // given(TeamMapper.toTeamJoinResponse(team)).willReturn(expectedResponse); // TeamMapper.toTeamJoinResponse -> static

    // when
    TeamJoinResponse result = teamService.joinTeam(newMemberDetails, teamCode, teamPassword);

    // then
    assertThat(result).isNotNull();
    assertThat(result.id).isEqualTo(team.getId());
    assertThat(result.teamName).isEqualTo(team.getName());
    assertThat(result.leaderId).isEqualTo(team.getLeader().getId());
    assertThat(result.teamCode).isEqualTo(team.getTeamCode());

    // 팀 멤버 저장
    ArgumentCaptor<TeamMember> teamMemberCaptor = ArgumentCaptor.forClass(TeamMember.class);
    verify(teamMemberRepository).save(teamMemberCaptor.capture());
    TeamMember capturedTeamMember = teamMemberCaptor.getValue();
    assertThat(capturedTeamMember.getMember()).isEqualTo(newMember);
    assertThat(capturedTeamMember.getTeam()).isEqualTo(team);
  }

  @Test
  void 대시보드를get_대시보드Response_예상() {
    // Given
    Long teamId = 1L;
    Team team = mock(Team.class);

    Budget budget = mock(Budget.class);

    // PageRequest.of(0, 5, Sort.by("createdAt").descending())
    Pageable pageable = PageRequest.of(0, 5, org.springframework.data.domain.Sort.by("createdAt").descending());

    List<Expense> expenseList = new ArrayList<>();
    Page<Expense> expensePage = new PageImpl<>(expenseList, pageable, expenseList.size());


    TeamDashboardResponse expectedResponse = mock(TeamDashboardResponse.class);

    when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));
    when(budgetRepository.findByTeamId(teamId)).thenReturn(Optional.of(budget));
    when(expenseRepository.findByTeamId(teamId, pageable)).thenReturn(expensePage);

    // When
    TeamDashboardResponse result = teamService.getTeamDashboard(teamId);

    // Then

    assertThat(result).isNotNull();
    verify(teamRepository).findById(teamId);
    verify(budgetRepository).findByTeamId(teamId);
    verify(expenseRepository).findByTeamId(teamId, pageable);
    verify(expenseRepository).findByTeamId(teamId, pageable);
  }

  @Test
  void getTeamDashboard_WhenTeamNotFound_ShouldThrowException() {
    // Given
    Long teamId = 1L;
    when(teamRepository.findById(teamId)).thenReturn(Optional.empty());

    // When & Then
    assertThrows(CustomLogicException.class, () -> teamService.getTeamDashboard(teamId));
    verify(teamRepository).findById(teamId);
  }

}
