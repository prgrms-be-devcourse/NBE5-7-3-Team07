package com.luckyseven.backend.domain.team.dto

import com.luckyseven.backend.domain.team.entity.Team

data class TeamListResponse(
    val id: Long?,
    val name: String?,
    val teamCode: String?
) {
    companion object{
        fun toTeamListResponse(team: Team?): TeamListResponse?{
            return TeamListResponse(
                id = team?.id,
                name = team?.name,
                teamCode = team?.teamCode
            )
        }
    }
}