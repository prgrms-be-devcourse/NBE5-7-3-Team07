package com.luckyseven.backend.domain.team.dto;

public record TeamCreateResponse(
    Long id,
    String name,
    String teamCode,
    Long leaderId
) {
  // 빌더 메서드 추가
  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private Long id;
    private String name;
    private String teamCode;
    private Long leaderId;

    public Builder id(Long id) {
      this.id = id;
      return this;
    }

    public Builder name(String name) {
      this.name = name;
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

    public TeamCreateResponse build() {
      return new TeamCreateResponse(id, name, teamCode, leaderId);
    }
  }
}