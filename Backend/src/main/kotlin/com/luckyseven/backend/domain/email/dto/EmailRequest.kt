package com.luckyseven.backend.domain.email.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern

data class EmailRequest(
    @field:NotNull
    @field:Email
    @field:Schema(description = "이메일")
    @field:Pattern(regexp = "^[a-zA-Z0-9]+@[a-zA-Z0-9]+\\.[a-zA-Z0-9]+$")
    val email: String
) 