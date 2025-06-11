package com.luckyseven.backend.domain.settlements.dao;

import com.luckyseven.backend.domain.settlements.dto.SettlementSearchCondition;
import com.luckyseven.backend.domain.settlements.entity.Settlement;
import org.springframework.data.jpa.domain.Specification;

public class SettlementSpecification {

  public static Specification<Settlement> hasTeamId(Long teamId) {
    return (root, query, cb) -> teamId == null ? null :
        cb.equal(root.get("expense").get("team").get("id"), teamId);
  }

  public static Specification<Settlement> hasPayerId(Long payerId) {
    return (root, query, criteriaBuilder) -> payerId == null ? null
        : criteriaBuilder.equal(root.get("payer").get("id"), payerId);
  }

  public static Specification<Settlement> hasSettlerId(Long settlerId) {
    return (root, query, criteriaBuilder) -> settlerId == null ? null
        : criteriaBuilder.equal(root.get("settler").get("id"), settlerId);
  }

  public static Specification<Settlement> hasExpenseId(Long expenseId) {
    return (root, query, criteriaBuilder) -> expenseId == null ? null
        : criteriaBuilder.equal(root.get("expense").get("id"), expenseId);
  }

  public static Specification<Settlement> isSettled(Boolean isSettled) {
    return (root, query, criteriaBuilder) -> isSettled == null ? null
        : criteriaBuilder.equal(root.get("isSettled"), isSettled);
  }

  public static Specification<Settlement> createSpecification(Long teamId,
      SettlementSearchCondition condition) {
    return Specification
        .where(SettlementSpecification.hasTeamId(teamId))
        .and(hasPayerId(condition.payerId))
        .and(hasSettlerId(condition.settlerId()))
        .and(hasExpenseId(condition.expenseId))
        .and(isSettled(condition.isSettled));
  }
}
