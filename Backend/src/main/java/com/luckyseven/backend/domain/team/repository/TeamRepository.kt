    package com.luckyseven.backend.domain.team.repository

    import com.luckyseven.backend.domain.team.entity.Team
    import org.springframework.data.jpa.repository.JpaRepository
    import org.springframework.data.jpa.repository.Query
    import org.springframework.data.repository.query.Param
    import org.springframework.stereotype.Repository
    import java.util.*

    @Repository
    interface TeamRepository : JpaRepository<Team?, Long?> {
        fun findByTeamCode(teamCode: String): Optional<Team>

        @Query("select t from Team t join fetch t.budget where t.id = :teamId")
        fun findTeamWithBudget(@Param("teamId") teamId: Long): Optional<Team>
    }
