package com.luckyseven.backend.domain.team.dto

import com.luckyseven.backend.domain.team.entity.Team

data class TeamJoinResponse(
    val id: Long?,
    val teamName: String?,
    val teamCode: String?,
    val leaderId: Long?
) {
    companion object {
        fun toTeamJoinResponse(team: Team?): TeamJoinResponse? {
            return team?.let {
                TeamJoinResponse(
                    id = it.id,
                    teamName = it.name,
                    teamCode = it.teamCode,
                    leaderId = it.leader?.id
                )
            }
        }
    }
}