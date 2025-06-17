package com.luckyseven.backend.domain.team.dto

data class TeamJoinResponse(
    val id: Long?,
    val teamName: String,
    val teamCode: String,
    val leaderId: Long?
)