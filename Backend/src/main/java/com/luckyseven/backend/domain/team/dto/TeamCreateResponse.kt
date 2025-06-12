package com.luckyseven.backend.domain.team.dto

import com.luckyseven.backend.domain.team.entity.Team

data class TeamCreateResponse(
    val id: Long?,
    val name: String?,
    val teamCode: String?,
    val leaderId: Long?
)