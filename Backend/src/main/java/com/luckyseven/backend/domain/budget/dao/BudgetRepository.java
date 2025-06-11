package com.luckyseven.backend.domain.budget.dao;

import com.luckyseven.backend.domain.budget.entity.Budget;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BudgetRepository extends JpaRepository<Budget, Long> {
  Optional<Budget> findByTeamId(Long teamId);

  @Query("SELECT b.updatedAt FROM Budget b WHERE b.team.id = :teamId")
  Optional<LocalDateTime> findUpdatedAtByTeamId(@Param("teamId") Long teamId);
}
