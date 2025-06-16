package com.luckyseven.backend.domain.team.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.Size

data class TeamMemberDto(
    val id: Long,
    val teamId: Long,

    @field:Size(min = 2, max = 30, message = "팀 이름은 2자 이상 30자 이하여야 합니다")
    val teamName: String,

    val memberId: Long,

    @field:Size(
        min = 2,
        max = 30,
        message = "회원 이름은 2자 이상 30자 이하여야 합니다"
    ) val memberNickName: String,

    @field:Email(message = "올바른 이메일 형식이 아닙니다")
    val memberEmail: String,

    val role: String
) {
    companion object {

    }
}
