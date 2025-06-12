package com.luckyseven.backend.domain.member.dto

import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern

//TODO:테스트 코드 작성
data class LoginMemberRequest(
    @field:NotNull
    @field:Pattern(regexp = "^[a-zA-Z0-9]+@[a-zA-Z0-9]+\\.[a-zA-Z0-9]+$")
    val email: String,
    
    @field:NotNull
    val password: String
) 