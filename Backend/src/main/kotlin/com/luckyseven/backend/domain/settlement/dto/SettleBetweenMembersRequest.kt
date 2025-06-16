package com.luckyseven.backend.domain.settlement.dto

import jakarta.validation.constraints.Positive

data class SettleBetweenMembersRequest(
    @field:Positive
    val from: Long,

    @field:Positive
    val to: Long
)
