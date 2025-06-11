package com.luckyseven.backend.domain.member.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
//TODO:테스트 코드 작성
public record LoginMemberRequest(
    @NotNull
    @Pattern(regexp = "^[a-zA-Z0-9]+@[a-zA-Z0-9]+\\.[a-zA-Z0-9]+$")
    String email,
    @NotNull
    String password
) {

}
