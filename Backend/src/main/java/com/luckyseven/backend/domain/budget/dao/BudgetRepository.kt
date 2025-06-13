package com.luckyseven.backend.domain.budget.dao

import com.luckyseven.backend.domain.budget.entity.Budget
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface BudgetRepository : JpaRepository<Budget, Long> {
    fun findByTeamId(teamId: Long): Budget?
}
