package com.luckyseven.backend.domain.member.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.luckyseven.backend.domain.team.entity.Team;
import com.luckyseven.backend.domain.team.entity.TeamMember;
import com.luckyseven.backend.sharedkernel.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "member")
public class Member extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String email;

  private String password;

  private String nickname;

  @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
  @JsonIgnore
  @Schema(hidden = true)
  private List<TeamMember> teamMembers = new ArrayList<>();


  @OneToMany(mappedBy = "leader")
  @JsonIgnore
  @Schema(hidden = true)
  private List<Team> leadingTeams = new ArrayList<>();

  // 양방향 연관관계 유지를 위한 메서드 추가
  public void addLeadingTeam(Team team) {
    this.leadingTeams.add(team);
    if (!this.equals(team.getLeader())) {
      team.setLeader(this);
    }
  }

  public void removeLeadingTeam(Team team) {
    this.leadingTeams.remove(team);
    if (!this.equals(team.getLeader())) {
      team.setLeader(null);
    }
  }

  public void addTeamMember(TeamMember teamMember) {
    this.teamMembers.add(teamMember);
    teamMember.setMember(this);
  }
// 테스트 코드 위해 임시 비활성화
//  @Builder
//  public Member(String email,String password , String nickname){
//    this.email = email;
//    this.password = password;
//    this.nickname = nickname;
//  }

  @Builder
  public Member(Long id, String email,String password , String nickname){
    this.id = id;
    this.email = email;
    this.password = password;
    this.nickname = nickname;
  }
}

