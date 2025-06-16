package com.luckyseven.backend.domain.team.dto

data class TeamCreateResponse(
    val id: Long?,
    val name: String,
    val teamCode: String,
    val leaderId: Long?
)