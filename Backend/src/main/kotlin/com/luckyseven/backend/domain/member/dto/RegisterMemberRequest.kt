package com.luckyseven.backend.domain.member.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class RegisterMemberRequest(
    @field:NotNull
    @field:Email
    @field:Schema(description = "이메일")
    @field:Pattern(regexp = "^[a-zA-Z0-9]+@[a-zA-Z0-9]+\\.[a-zA-Z0-9]+$")
    val email: String,
    
    @field:NotNull
    @field:Size(min = 6, max = 20, message = "팀 비밀번호는 6자 이상 20자 이하여야 합니다")
    @field:Pattern(
        regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d@$!%*#?&]{6,}$",
        message = "비밀번호는 최소 하나의 문자와 숫자를 포함해야 합니다"
    )
    @field:Schema(description = "비밀번호")
    val password: String,
    
    @field:NotNull
    @field:Size(min = 6, max = 20, message = "팀 비밀번호는 6자 이상 20자 이하여야 합니다")
    @field:Pattern(
        regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d@$!%*#?&]{6,}$",
        message = "비밀번호는 최소 하나의 문자와 숫자를 포함해야 합니다"
    )
    @field:Schema(description = "비밀번호 확인")
    val checkPassword: String,
    
    @field:NotNull
    @field:Schema(description = "닉네임")
    val nickname: String
) 