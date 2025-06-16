package com.luckyseven.backend.domain.budget.util;

import com.luckyseven.backend.domain.budget.dto.BudgetAddRequest;
import com.luckyseven.backend.domain.budget.dto.BudgetCreateRequest;
import com.luckyseven.backend.domain.budget.dto.BudgetCreateResponse;
import com.luckyseven.backend.domain.budget.dto.BudgetReadResponse;
import com.luckyseven.backend.domain.budget.dto.BudgetUpdateRequest;
import com.luckyseven.backend.domain.budget.dto.BudgetUpdateResponse;
import com.luckyseven.backend.domain.budget.entity.Budget;
import com.luckyseven.backend.domain.budget.entity.CurrencyCode;
import com.luckyseven.backend.domain.member.entity.Member;
import com.luckyseven.backend.domain.member.service.utill.MemberDetails;
import com.luckyseven.backend.domain.team.entity.Team;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class TestUtils {

  public static MemberDetails genMemberDetails() {
    return new MemberDetails(
        1L,
        "user1111",
        "user1@user.com",
        "user1"
    );
  }

  // 테스트에서 인증된 사용자처럼 보이게 설정
  public static void genAuthentication(MemberDetails member) {
    Authentication auth = new UsernamePasswordAuthenticationToken(
        member, null, member.getAuthorities()
    );
    SecurityContextHolder.getContext().setAuthentication(auth);
  }

  public static BudgetCreateRequest genBudgetCreateReq() {
    return new BudgetCreateRequest(
        BigDecimal.valueOf(100000),
        true,
        BigDecimal.valueOf(1393.7),
        CurrencyCode.USD
    );
  }

  public static BudgetCreateResponse genBudgetCreateResp() {
    return new BudgetCreateResponse(
        1L,
        LocalDateTime.now(),
        1L,
        BigDecimal.valueOf(100000),
        BigDecimal.valueOf(1393.7),
        BigDecimal.valueOf(71.75)
    );
  }

  public static BudgetUpdateRequest genBudgetUpdateReq() {
    return new BudgetUpdateRequest(
        BigDecimal.valueOf(80000),
        true,
        BigDecimal.valueOf(1393.7)
    );
  }

  public static BudgetUpdateResponse genBudgetUpdateResp() {
    return new BudgetUpdateResponse(
        1L,
        LocalDateTime.now(),
        1L,
        BigDecimal.valueOf(80000),
        CurrencyCode.USD,
        BigDecimal.valueOf(1393.7),
        BigDecimal.valueOf(57.40)
    );
  }

  public static BudgetAddRequest genBudgetAddReq() {
    return new BudgetAddRequest(
        BigDecimal.valueOf(50000),
        false,
        null
    );
  }

  public static BudgetUpdateResponse genBudgetUpdateRespAfterAdd() {
    return new BudgetUpdateResponse(
        1L,
        LocalDateTime.now(),
        1L,
        BigDecimal.valueOf(150000),
        CurrencyCode.USD,
        BigDecimal.valueOf(1393.7),
        BigDecimal.valueOf(71.75)
    );
  }

  public static BudgetReadResponse genBudgetReadResp() {
    return new BudgetReadResponse(
        1L,
        LocalDateTime.now(),
        1L,
        BigDecimal.valueOf(100000),
        BigDecimal.valueOf(100000),
        CurrencyCode.USD,
        null,
        null
    );
  }

  public static Member genMember() {
    return new Member(1L, "user1@user.com", "user1111", "user1");
  }

  public static Team genTeam() {
    return new Team(1L, "user1", "lucky", "abcd1234", genMember(), null, null);
  }

  public static Budget genBudget() {
    Member leader = genMember();
    return Budget.builder()
        .team(genTeam())
        .totalAmount(BigDecimal.valueOf(100000))
        .setBy(leader.getId())
        .balance(BigDecimal.valueOf(100000))
        .foreignBalance(BigDecimal.valueOf(71.75))
        .foreignCurrency(CurrencyCode.USD)
        .avgExchangeRate(BigDecimal.valueOf(1393.7))
        .build();
  }

}
