package com.luckyseven.backend.domain.team.util;

import com.luckyseven.backend.domain.member.entity.Member;
import com.luckyseven.backend.domain.team.entity.Team;
import com.luckyseven.backend.domain.team.entity.TeamMember;

/**
 * 테스트를 위한 엔티티 생성 유틸리티 클래스
 * - 엔티티 ID 설정 및 테스트용 객체 생성을 담당
 */
public class TestEntityBuilder {

  /**
   * ID가 설정된 Team 객체 생성
   */
  public static Team createTeamWithId(Long id, String name, String code, String password) {
    Team team = Team.builder()
        .name(name)
        .teamCode(code)
        .teamPassword(password)
        .build();

    setEntityId(team, id);
    return team;
  }

  /**
   * ID가 설정된 TeamMember 객체 생성
   */
  public static TeamMember createTeamMemberWithId(Long id, Team team, Member member) {
    TeamMember teamMember = new TeamMember();
    teamMember.setTeam(team);
    teamMember.setMember(member);

    setEntityId(teamMember, id);
    return teamMember;
  }

  /**
   * ID가 설정된 Member 객체 생성
   */
  public static Member createMemberWithId(Long id, String email, String nickName) {
    Member member = Member.builder()
        .email(email)
        .nickname(nickName)
        .build();

    setEntityId(member, id);
    return member;
  }

  /**
   * 엔티티 객체의 ID 필드를 리플렉션을 사용해 설정
   * - 상속 계층 구조를 스캔하여 id 필드를 찾아 설정
   */
  private static void setEntityId(Object entity, Long id) {
    Class<?> currentClass = entity.getClass();

    // 상속 계층 구조를 전부 스캔
    while (currentClass != null && !currentClass.equals(Object.class)) {
      try {
        // 현재 클래스에서 id 필드 찾기 시도
        java.lang.reflect.Field field = currentClass.getDeclaredField("id");
        field.setAccessible(true);
        field.set(entity, id);
        return; // 성공적으로 설정했으면 메서드 종료
      } catch (NoSuchFieldException e) {
        // 현재 클래스에 id 필드가 없으면 상위 클래스로 이동
        currentClass = currentClass.getSuperclass();
      } catch (Exception e) {
        throw new RuntimeException("ID 필드 설정 중 오류 발생", e);
      }
    }

    throw new RuntimeException("상속 계층 구조에서 id 필드를 찾을 수 없습니다: " + entity.getClass().getName());
  }
}