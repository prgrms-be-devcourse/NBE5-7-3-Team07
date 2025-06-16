package com.luckyseven.backend.domain.budget.dao

import com.luckyseven.backend.domain.budget.entity.Budget
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDateTime

interface BudgetRepository : JpaRepository<Budget, Long> {
    fun findByTeamId(teamId: Long): Budget?

    @Query("SELECT b.updatedAt FROM Budget b WHERE b.team.id = :teamId")
    fun findUpdatedAtByTeamId(@Param("teamId") teamId: Long): LocalDateTime?
}
