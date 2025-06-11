package com.luckyseven.backend.domain.team.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record TeamMemberDto(
    Long id,
    Long teamId,

    @Size(min = 2, max = 30, message = "팀 이름은 2자 이상 30자 이하여야 합니다")
    String teamName,

    Long memberId,

    @Size(min = 2, max = 30, message = "회원 이름은 2자 이상 30자 이하여야 합니다")
    String memberNickName,

    @Email(message = "올바른 이메일 형식이 아닙니다")
    String memberEmail,

    String role
) {

  // 빌더 메서드 추가
  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private Long id;
    private Long teamId;
    private String teamName;
    private Long memberId;
    private String memberNickName;
    private String memberEmail;
    private String role;

    public Builder id(Long id) {
      this.id = id;
      return this;
    }

    public Builder teamId(Long teamId) {
      this.teamId = teamId;
      return this;
    }

    public Builder teamName(String teamName) {
      this.teamName = teamName;
      return this;
    }

    public Builder memberId(Long memberId) {
      this.memberId = memberId;
      return this;
    }

    public Builder memberNickName(String memberNickName) {
      this.memberNickName = memberNickName;
      return this;
    }

    public Builder memberEmail(String memberEmail) {
      this.memberEmail = memberEmail;
      return this;
    }

    public Builder role(String role) {
      this.role = role;
      return this;
    }

    public TeamMemberDto build() {
      return new TeamMemberDto(id, teamId, teamName, memberId, memberNickName, memberEmail, role);
    }
  }
}
