package com.luckyseven.backend.domain.team.repository

import com.luckyseven.backend.domain.member.entity.Member
import com.luckyseven.backend.domain.team.entity.Team
import com.luckyseven.backend.domain.team.entity.TeamMember
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface TeamMemberRepository : JpaRepository<TeamMember, Long> {
    fun existsByTeamAndMember(team: Team, member: Member): Boolean

    fun findByTeamId(id: Long): List<TeamMember>

    fun findByMemberId(memberId: Long): List<TeamMember>
}
