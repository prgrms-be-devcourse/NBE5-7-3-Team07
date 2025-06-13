package com.luckyseven.backend.domain.settlement.dao

import com.luckyseven.backend.domain.settlement.entity.Settlement
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface SettlementRepository : JpaRepository<Settlement, Long>,
    JpaSpecificationExecutor<Settlement?> {

    @EntityGraph(attributePaths = ["settler", "payer"])
    fun findWithSettlerAndPayerById(id: Long): Settlement?

    @Query("SELECT s From Settlement s join s.expense e where e.team.id = :teamId")
    fun findAllByTeamId(teamId: Long): List<Settlement>
}
