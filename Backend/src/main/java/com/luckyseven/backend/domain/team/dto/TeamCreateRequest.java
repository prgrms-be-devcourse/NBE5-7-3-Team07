package com.luckyseven.backend.domain.team.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record TeamCreateRequest(
    @NotBlank(message = "팀 이름은 필수입니다")
    @Pattern(regexp = "^[가-힣a-zA-Z0-9\\s]*$", message = "팀 이름은 한글, 영문, 숫자만 사용 가능합니다")
    String name,

    @NotBlank(message = "팀 비밀번호는 필수입니다")
    @Size(min = 6, max = 20, message = "팀 비밀번호는 6자 이상 20자 이하여야 합니다")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d@$!%*#?&]{6,}$",
        message = "비밀번호는 최소 하나의 문자와 숫자를 포함해야 합니다")
    String teamPassword
) {
  // 필요한 경우 빌더 메서드 추가
  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private String name;
    private String teamPassword;

    public Builder name(String name) {
      this.name = name;
      return this;
    }

    public Builder teamPassword(String teamPassword) {
      this.teamPassword = teamPassword;
      return this;
    }

    public TeamCreateRequest build() {
      return new TeamCreateRequest(name, teamPassword);
    }
  }
}
