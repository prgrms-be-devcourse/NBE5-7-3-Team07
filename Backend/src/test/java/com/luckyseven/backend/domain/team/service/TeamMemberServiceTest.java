package com.luckyseven.backend.domain.team.service;

import com.luckyseven.backend.domain.member.entity.Member;
import com.luckyseven.backend.domain.member.repository.MemberRepository;
import com.luckyseven.backend.domain.member.service.utill.MemberDetails;
import com.luckyseven.backend.domain.team.dto.TeamMemberDto;
import com.luckyseven.backend.domain.team.entity.Team;
import com.luckyseven.backend.domain.team.entity.TeamMember;
import com.luckyseven.backend.domain.team.repository.TeamMemberRepository;
import com.luckyseven.backend.domain.team.repository.TeamRepository;
import com.luckyseven.backend.domain.team.util.TeamMemberMapper;
import com.luckyseven.backend.domain.team.util.TestEntityBuilder;
import com.luckyseven.backend.sharedkernel.exception.CustomLogicException;
import com.luckyseven.backend.sharedkernel.exception.ExceptionCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TeamMemberServiceTest {

  @Mock
  private TeamRepository teamRepository;

  @Mock
  private TeamMemberRepository teamMemberRepository;

  @Mock
  private MemberRepository memberRepository; // Added MemberRepository mock

  @InjectMocks
  private TeamMemberService teamMemberService;

  private MemberDetails mockMemberDetails;
  private Member actingMember;
  private Member regularMember;
  private Member teamLeaderMember;

  @BeforeEach
  void setUp() {
    mockMemberDetails = mock(MemberDetails.class);
    actingMember = TestEntityBuilder.createMemberWithId(1L, "acting@example.com", "Acting User");
    teamLeaderMember = TestEntityBuilder.createMemberWithId(2L, "leader@example.com",
        "Team Leader");
    regularMember = TestEntityBuilder.createMemberWithId(3L, "member@example.com",
        "Regular Member");

  }

  /**
   * 팀 ID로 팀 멤버 목록을 조회하는 기능 테스트 - 팀이 존재할 경우 TeamMemberDto 리스트를 반환하는지 확인 - DTO 변환이 제대로 이루어지는지 검증
   */
  @Test
  void getTeamMemberByTeamId_팀이존재하면_멤버목록반환() {
    // 준비
    Long teamId = 1L;
    Long memberId = 2L; // 체크할 유저
    Long teamMemberId = 101L;

    Team team = TestEntityBuilder.createTeamWithId(teamId, "테스트 팀", "TEST-001", "pass123");
    team.setLeader(teamLeaderMember);
    Member memberInTeam = TestEntityBuilder.createMemberWithId(memberId, "test@example.com", "홍길동");
    TeamMember teamMember = TestEntityBuilder.createTeamMemberWithId(teamMemberId, team,
        memberInTeam);

    List<TeamMember> teamMembers = Arrays.asList(teamMember);

    // DTO 설정 TeamMemberMapper.toDtoList -> static
    // 정적 메서드의 실제 반환값과 비교하여 검증
    List<TeamMemberDto> expectedDtos = TeamMemberMapper.toDtoList(teamMembers);

    // Mock 설정
    when(teamRepository.existsById(teamId)).thenReturn(true);
    when(teamMemberRepository.findByTeamId(teamId)).thenReturn(teamMembers);

    // 실행
    List<TeamMemberDto> result = teamMemberService.getTeamMemberByTeamId(teamId);

    // 검증
    assertEquals(1, result.size());
    assertEquals(expectedDtos.getFirst().id, result.getFirst().id);
    assertEquals(expectedDtos.getFirst().teamId, result.getFirst().teamId);
    assertEquals(expectedDtos.getFirst().teamName, result.getFirst().teamName);
    verify(teamRepository).existsById(teamId);
    verify(teamMemberRepository).findByTeamId(teamId);
  }

  /**
   * 팀 ID로 팀 멤버 목록을 조회할 때 팀이 존재하지 않는 경우 테스트 - 예외가 발생하는지 확인
   */
  @Test
  void getTeamMemberByTeamId_팀이없으면_예외발생() {
    // 준비
    Long teamId = 1L;
    when(teamRepository.existsById(teamId)).thenReturn(false);

    // 실행 및 검증
    CustomLogicException exception = assertThrows(CustomLogicException.class, () -> {
      teamMemberService.getTeamMemberByTeamId(teamId);
    });
    assertEquals(ExceptionCode.TEAM_NOT_FOUND, exception.getExceptionCode());

    verify(teamRepository).existsById(teamId);
    verifyNoInteractions(teamMemberRepository); // Mapper -> static
  }

  /**
   * 팀 멤버 삭제 기능 테스트 - 유효한 요청일 경우 (팀 리더가 다른 멤버 삭제) 멤버가 삭제되는지 확인
   */
  @Test
  void removeTeamMember_유효한요청_리더가멤버삭제_성공() {
    // 준비
    Long teamId = 1L;
    Long teamMemberIdToRemove = 101L; // ID of TeamMember entity to remove

    // Acting member is the team leader
    when(mockMemberDetails.getId()).thenReturn(teamLeaderMember.getId());
    when(memberRepository.findById(teamLeaderMember.getId())).thenReturn(
        Optional.of(teamLeaderMember));

    Team team = TestEntityBuilder.createTeamWithId(teamId, "테스트 팀", "TEST-001", "pass123");
    team.setLeader(teamLeaderMember); // actingMember == leader

    // Member to be removed (regularMember)
    TeamMember teamMemberToRemove = TestEntityBuilder.createTeamMemberWithId(teamMemberIdToRemove,
        team, regularMember);

    when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));
    when(teamMemberRepository.findById(teamMemberIdToRemove)).thenReturn(
        Optional.of(teamMemberToRemove));

    // 실행
    teamMemberService.removeTeamMember(mockMemberDetails, teamId, teamMemberIdToRemove);

    // 검증
    verify(memberRepository).findById(teamLeaderMember.getId());
    verify(teamRepository).findById(teamId);
    verify(teamMemberRepository).findById(teamMemberIdToRemove);
    verify(teamMemberRepository).deleteById(teamMemberIdToRemove);
  }

  /**
   * 팀 멤버 삭제 시 팀이 존재하지 않는 경우 테스트 - 예외가 발생하는지 확인
   */
  @Test
  void removeTeamMember_팀이없으면_예외발생() {
    // 준비
    Long teamId = 1L;
    Long teamMemberId = 101L;

    // Acting member setup
    when(mockMemberDetails.getId()).thenReturn(actingMember.getId());
    when(memberRepository.findById(actingMember.getId())).thenReturn(Optional.of(actingMember));
    when(teamRepository.findById(teamId)).thenReturn(Optional.empty()); // Team not found

    // 실행 및 검증
    CustomLogicException exception = assertThrows(CustomLogicException.class, () -> {
      teamMemberService.removeTeamMember(mockMemberDetails, teamId, teamMemberId);
    });
    assertEquals(ExceptionCode.TEAM_NOT_FOUND, exception.getExceptionCode());

    verify(memberRepository).findById(actingMember.getId());
    verify(teamRepository).findById(teamId);
    verifyNoInteractions(teamMemberRepository);
  }

  /**
   * 팀 멤버 삭제 시 해당 팀멤버가 존재하지 않는 경우 테스트 - 예외가 발생하는지 확인
   */
  @Test
  void removeTeamMember_삭제할팀멤버가없으면_예외발생() {
    // 준비
    Long teamId = 1L;
    Long teamMemberId = 101L;

    Team team = TestEntityBuilder.createTeamWithId(teamId, "테스트 팀", "TEST-001", "pass123");
    team.setLeader(actingMember); // acting member == leader

    when(mockMemberDetails.getId()).thenReturn(actingMember.getId());
    when(memberRepository.findById(actingMember.getId())).thenReturn(Optional.of(actingMember));
    when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));
    when(teamMemberRepository.findById(teamMemberId)).thenReturn(
        Optional.empty()); // TㄴeamMember not found

    // 실행 및 검증
    CustomLogicException exception = assertThrows(CustomLogicException.class, () -> {
      teamMemberService.removeTeamMember(mockMemberDetails, teamId, teamMemberId);
    });
    assertEquals(ExceptionCode.TEAM_MEMBER_NOT_FOUND, exception.getExceptionCode());

    verify(memberRepository).findById(actingMember.getId());
    verify(teamRepository).findById(teamId);
    verify(teamMemberRepository).findById(teamMemberId);
  }

  /**
   * ㅇ 팀 멤버 삭제 시 해당 멤버가 다른 팀에 속한 경우 테스트 - 예외가 발생하는지 확인
   */
  @Test
  void removeTeamMember_다른팀멤버면_예외발생() {
    // 준비
    Long targetTeamId = 1L;
    Long actualTeamIdOfMember = 2L;
    Long teamMemberId = 101L;

    // Acting member == 타겟 팀 리더
    when(mockMemberDetails.getId()).thenReturn(teamLeaderMember.getId());
    when(memberRepository.findById(teamLeaderMember.getId())).thenReturn(
        Optional.of(teamLeaderMember));

    Team targetTeam = TestEntityBuilder.createTeamWithId(targetTeamId, "타겟 팀", "TARGET-001",
        "pass123");
    targetTeam.setLeader(teamLeaderMember);

    Team actualTeam = TestEntityBuilder.createTeamWithId(actualTeamIdOfMember, "실제 팀", "ACTUAL-001",
        "pass456");
    // 제거하려는 멤버는 'targetTeam'이 아닌 'actualTeam'에 속해 있습니다.
    TeamMember teamMemberInWrongTeam = TestEntityBuilder.createTeamMemberWithId(teamMemberId,
        actualTeam, regularMember);

    when(teamRepository.findById(targetTeamId)).thenReturn(Optional.of(targetTeam));
    when(teamMemberRepository.findById(teamMemberId)).thenReturn(
        Optional.of(teamMemberInWrongTeam));

    // 실행 및 검증
    CustomLogicException exception = assertThrows(CustomLogicException.class, () -> {
      teamMemberService.removeTeamMember(mockMemberDetails, targetTeamId, teamMemberId);
    });
    assertEquals(ExceptionCode.NOT_TEAM_MEMBER, exception.getExceptionCode());

    verify(memberRepository).findById(teamLeaderMember.getId());
    verify(teamRepository).findById(targetTeamId);
    verify(teamMemberRepository).findById(teamMemberId);
  }

  /**
   * 팀 멤버 삭제 시 요청자가 팀 리더가 아닌 경우 테스트 - 예외 발생 (ROLE_FORBIDDEN)
   */
  @Test
  void removeTeamMember_요청자가리더아니면_예외발생() {
    // 준비
    Long teamId = 1L;
    Long teamMemberIdToRemove = 101L;

    // Acting member != team leader
    // teamLeaderMember (ID 2L) == leader, actingMember (ID 1L) 가 delete 하려고 하는 상황
    when(mockMemberDetails.getId()).thenReturn(actingMember.getId()); // actingMember == 1L
    when(memberRepository.findById(actingMember.getId())).thenReturn(Optional.of(actingMember));

    Team team = TestEntityBuilder.createTeamWithId(teamId, "테스트 팀", "TEST-001", "pass123");
    team.setLeader(teamLeaderMember); // Leader == teamLeaderMember (2L)

    TeamMember teamMemberToRemove = TestEntityBuilder.createTeamMemberWithId(teamMemberIdToRemove,
        team, regularMember);

    when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));
    when(teamMemberRepository.findById(teamMemberIdToRemove)).thenReturn(
        Optional.of(teamMemberToRemove));

    // 실행 및 검증
    CustomLogicException exception = assertThrows(CustomLogicException.class, () -> {
      teamMemberService.removeTeamMember(mockMemberDetails, teamId, teamMemberIdToRemove);
    });
    assertEquals(ExceptionCode.ROLE_FORBIDDEN, exception.getExceptionCode());

    verify(memberRepository).findById(actingMember.getId());
    verify(teamRepository).findById(teamId);
    verify(teamMemberRepository).findById(teamMemberIdToRemove);
    verify(teamMemberRepository, never()).deleteById(anyLong());
  }

  /**
   * 팀 멤버 삭제 시 팀 리더 자신을 삭제하려는 경우 테스트 - 예외 발생 (METHOD_NOT_ALLOWED)
   */
  @Test
  void removeTeamMember_리더가자신을삭제하려하면_예외발생() {
    // 준비
    Long teamId = 1L;
    Long teamMemberIdOfLeader = 101L;

    // Acting member == team leader
    when(mockMemberDetails.getId()).thenReturn(teamLeaderMember.getId());
    when(memberRepository.findById(teamLeaderMember.getId())).thenReturn(
        Optional.of(teamLeaderMember));

    Team team = TestEntityBuilder.createTeamWithId(teamId, "테스트 팀", "TEST-001", "pass123");
    team.setLeader(teamLeaderMember);

    // 리더가 자신을 삭제
    TeamMember leaderAsTeamMember = TestEntityBuilder.createTeamMemberWithId(teamMemberIdOfLeader,
        team, teamLeaderMember);

    when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));
    when(teamMemberRepository.findById(teamMemberIdOfLeader)).thenReturn(
        Optional.of(leaderAsTeamMember));

    // 실행 및 검증
    CustomLogicException exception = assertThrows(CustomLogicException.class, () -> {
      teamMemberService.removeTeamMember(mockMemberDetails, teamId, teamMemberIdOfLeader);
    });
    assertEquals(ExceptionCode.METHOD_NOT_ALLOWED, exception.getExceptionCode());

    verify(memberRepository).findById(teamLeaderMember.getId());
    verify(teamRepository).findById(teamId);
    verify(teamMemberRepository).findById(teamMemberIdOfLeader);
    verify(teamMemberRepository, never()).deleteById(anyLong());
  }

  /**
   * 팀 멤버 삭제 시 요청한 사용자가 존재하지 않는 경우 테스트 - 예외 발생 (MEMBER_ID_NOTFOUND)
   */
  @Test
  void removeTeamMember_요청멤버가없으면_예외발생() {
    // 준비
    Long teamId = 1L;
    Long teamMemberId = 101L;
    Long nonExistentMemberId = 999L;

    when(mockMemberDetails.getId()).thenReturn(nonExistentMemberId);
    when(memberRepository.findById(nonExistentMemberId)).thenReturn(Optional.empty()); // 요청 멤버가 없음

    // 실행 및 검증
    CustomLogicException exception = assertThrows(CustomLogicException.class, () -> {
      teamMemberService.removeTeamMember(mockMemberDetails, teamId, teamMemberId);
    });
    assertEquals(ExceptionCode.MEMBER_ID_NOTFOUND, exception.getExceptionCode());

    verify(memberRepository).findById(nonExistentMemberId);
    verifyNoInteractions(teamRepository);
    verifyNoInteractions(teamMemberRepository);
  }
}
