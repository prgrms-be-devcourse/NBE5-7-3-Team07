package com.luckyseven.backend.domain.team.util

import com.luckyseven.backend.domain.team.dto.TeamMemberDto
import com.luckyseven.backend.domain.team.entity.TeamMember

object TeamMemberMapper {
    /**
     * TeamMember 엔티티를 TeamMemberDto로 변환합니다.
     *
     * @param teamMember 변환할 팀멤버 엔티티
     * @return 변환된 팀멤버 DTO
     */
    fun toTeamMemberDto(teamMember: TeamMember?): TeamMemberDto? {
        if (teamMember == null) return null

        val team = teamMember.team
        val member = teamMember.member
        val leader = team?.leader
        val role = if (leader?.id == member?.id) "Leader" else "Member"

        return TeamMemberDto(
            id = teamMember.id,
            teamId = team?.id,
            teamName = team?.name,
            memberId = member?.id,
            memberNickName = member?.nickname,
            memberEmail = member?.email,
            role = role
        )
    }

    /**
     * TeamMember 엔티티 리스트를 TeamMemberDto 리스트로 변환합니다.
     *
     * @param teamMembers 변환할 팀멤버 엔티티 리스트
     * @return 변환된 팀멤버 DTO 리스트
     */
    fun toDtoList(teamMembers: List<TeamMember>?): List<TeamMemberDto> =
        teamMembers?.mapNotNull{ toTeamMemberDto(it)} ?: emptyList()
}
