package com.luckyseven.backend.domain.team.repository;

import com.luckyseven.backend.domain.member.entity.Member;
import com.luckyseven.backend.domain.team.entity.Team;
import com.luckyseven.backend.domain.team.entity.TeamMember;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {

  boolean existsByTeamAndMember(Team team, Member member);

  List<TeamMember> findByTeamId(Long id);

  List<TeamMember> findByMemberId(Long memberId);
}
