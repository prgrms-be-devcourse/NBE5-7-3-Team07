package com.luckyseven.backend.domain.team.dto

import com.luckyseven.backend.domain.team.entity.Team

data class TeamJoinResponse(
    val id: Long?,
    val teamName: String?,
    val teamCode: String?,
    val leaderId: Long?
)