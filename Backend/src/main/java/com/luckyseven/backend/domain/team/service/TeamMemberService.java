package com.luckyseven.backend.domain.team.service;

import com.luckyseven.backend.domain.member.repository.MemberRepository;
import com.luckyseven.backend.domain.member.service.utill.MemberDetails;
import com.luckyseven.backend.domain.team.dto.TeamMemberDto;
import com.luckyseven.backend.domain.member.entity.Member;
import com.luckyseven.backend.domain.team.entity.Team;
import com.luckyseven.backend.domain.team.entity.TeamMember;
import com.luckyseven.backend.domain.team.repository.TeamMemberRepository;
import com.luckyseven.backend.domain.team.repository.TeamRepository;
import com.luckyseven.backend.domain.team.util.TeamMemberMapper;
import com.luckyseven.backend.sharedkernel.exception.CustomLogicException;
import com.luckyseven.backend.sharedkernel.exception.ExceptionCode;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TeamMemberService {

  private final TeamRepository teamRepository;
  private final TeamMemberRepository teamMemberRepository;
  private final MemberRepository memberRepository;


  @Transactional(readOnly = true)
  public List<TeamMemberDto> getTeamMemberByTeamId(Long id) {
    if (!teamRepository.existsById(id)) {
      throw new CustomLogicException(ExceptionCode.TEAM_NOT_FOUND,
          "ID가 [%d]인 팀을 찾을 수 없습니다", id);
    }
    List<TeamMember> teamMembers = teamMemberRepository.findByTeamId(id);
    return TeamMemberMapper.toDtoList(teamMembers);
  }

  @Transactional
  public void removeTeamMember(MemberDetails memberDetails, Long teamId, Long teamMemberId) {
    Long memberId = memberDetails.getId();
    Member member = memberRepository.findById(memberId)
        .orElseThrow(() -> new CustomLogicException(ExceptionCode.MEMBER_ID_NOTFOUND, memberId));

    Team team = teamRepository.findById(teamId)
        .orElseThrow(() -> new CustomLogicException(ExceptionCode.TEAM_NOT_FOUND));

    // 팀 멤버 존재 여부 확인
    TeamMember teamMember = teamMemberRepository.findById(teamMemberId)
        .orElseThrow(() -> new CustomLogicException(ExceptionCode.TEAM_MEMBER_NOT_FOUND));

    if (!teamMember.getTeam().getId().equals(teamId)) {
      throw new CustomLogicException(ExceptionCode.NOT_TEAM_MEMBER,
          "팀 멤버 ID [%d]는 팀 ID [%d]에 속한 멤버가 아닙니다", teamMemberId, teamId);
    }

    if (!team.getLeader().getId().equals(member.getId())) {
      throw new CustomLogicException(ExceptionCode.ROLE_FORBIDDEN,
          "팀 멤버를 삭제할 권한이 없습니다. 팀 리더만 멤버를 삭제할 수 있습니다."
      );
    }
    if(team.getLeader().getId().equals(teamMember.getMember().getId())){
      throw new CustomLogicException(ExceptionCode.METHOD_NOT_ALLOWED,"팀 리더는 삭제 불가능합니다.");
    }
    // 팀 멤버 삭제
    team.removeTeamMember(teamMember);
    teamMemberRepository.deleteById(teamMemberId);
  }

}
