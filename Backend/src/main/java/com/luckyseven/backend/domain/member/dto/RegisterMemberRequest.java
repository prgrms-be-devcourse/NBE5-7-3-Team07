package com.luckyseven.backend.domain.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;


@Builder
public record RegisterMemberRequest(

    @NotNull
    @Email
    @Schema(description = "이메일")
    @Pattern(regexp = "^[a-zA-Z0-9]+@[a-zA-Z0-9]+\\.[a-zA-Z0-9]+$")
    String email,
    @NotNull
    @Size(min = 6, max = 20, message = "팀 비밀번호는 6자 이상 20자 이하여야 합니다")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d@$!%*#?&]{6,}$",
        message = "비밀번호는 최소 하나의 문자와 숫자를 포함해야 합니다")
    @Schema(description = "비밀번호")
    String password,
    @NotNull
    @NotNull
    @Size(min = 6, max = 20, message = "팀 비밀번호는 6자 이상 20자 이하여야 합니다")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d@$!%*#?&]{6,}$",
        message = "비밀번호는 최소 하나의 문자와 숫자를 포함해야 합니다")
    @Schema(description = "비밀번호 확인")
    String checkPassword,
    @NotNull
    @Schema(description = "닉네임")
    String nickname
) {
}
