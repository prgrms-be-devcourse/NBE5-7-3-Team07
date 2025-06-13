package com.luckyseven.backend.domain.team.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class TeamJoinRequest(
    @field:NotBlank(message = "팀 코드는 필수입니다")
    @field:Size(
        min = 8,
        max = 8,
        message = "팀 코드는 8자리여야 합니다"
    )
    @field:Pattern(
        regexp = "^[a-zA-Z0-9-]*$",
        message = "팀 코드는 영문자, 숫자, 하이픈만 사용 가능합니다"
    ) val teamCode: String,

    val teamPassword: String
)