package com.luckyseven.backend.domain.team.dto;

public record TeamJoinResponse(
    Long id,
    String teamName,
    String teamCode,
    Long leaderId
) {

  // 빌더 메서드 추가
  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private Long id;
    private String teamName;
    private String teamCode;
    private Long leaderId;

    public Builder id(Long id) {
      this.id = id;
      return this;
    }

    public Builder teamName(String teamName) {
      this.teamName = teamName;
      return this;
    }

    public Builder teamCode(String teamCode) {
      this.teamCode = teamCode;
      return this;
    }

    public Builder leaderId(Long leaderId) {
      this.leaderId = leaderId;
      return this;
    }

    public TeamJoinResponse build() {
      return new TeamJoinResponse(id, teamName, teamCode, leaderId);
    }
  }
}