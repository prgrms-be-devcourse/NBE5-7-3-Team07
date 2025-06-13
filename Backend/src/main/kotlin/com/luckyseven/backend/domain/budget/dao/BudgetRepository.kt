package com.luckyseven.backend.domain.budget.dao

import com.luckyseven.backend.domain.budget.entity.Budget
import org.springframework.data.jpa.repository.JpaRepository

interface BudgetRepository : JpaRepository<Budget, Long> {
    fun findByTeamId(teamId: Long): Budget?
}
