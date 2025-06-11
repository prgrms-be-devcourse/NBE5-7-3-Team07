package com.luckyseven.backend.domain.budget.dao;

import com.luckyseven.backend.domain.budget.entity.Budget;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BudgetRepository extends JpaRepository<Budget, Long> {
  Optional<Budget> findByTeamId(Long teamId);
}
