package com.luckyseven.backend.domain.team.repository

import com.luckyseven.backend.domain.team.entity.Team
import com.luckyseven.backend.domain.team.enums.TeamStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface TeamRepository : JpaRepository<Team, Long> {
    fun findByTeamCode(teamCode: String): Team?

    @Query("select t from Team t join fetch t.budget where t.id = :teamId")
    fun findTeamWithBudget(@Param("teamId") teamId: Long): Team?

    fun findByStatusAndDeletionScheduledAt(status: TeamStatus, deletionScheduledAt: LocalDateTime): List<Team>
}
