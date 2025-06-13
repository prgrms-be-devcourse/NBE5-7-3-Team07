package com.luckyseven.backend.domain.team.repository

import com.luckyseven.backend.domain.team.entity.Team
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface TeamRepository : JpaRepository<Team?, Long?> {
    fun findByTeamCode(teamCode: String): Optional<Team>

    @Query("SELECT t FROM Team t JOIN FETCH t.budget WHERE t.id = :teamId")
    fun findTeamWithBudget(teamId: Long): Team?
}
