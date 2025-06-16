package com.luckyseven.backend.domain.settlement.dao

import com.luckyseven.backend.domain.settlement.entity.Settlement
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.stream.Stream

@Repository
interface SettlementRepository : JpaRepository<Settlement, Long>,
    JpaSpecificationExecutor<Settlement> {

    @EntityGraph(attributePaths = ["settler", "payer"])
    fun findWithSettlerAndPayerById(id: Long): Settlement?

    @EntityGraph(attributePaths = ["settler", "payer"])
    @Query("SELECT s From Settlement s join s.expense e where e.team.id = :teamId and s.isSettled = false")
    fun findAllByTeamId(teamId: Long): Stream<Settlement>
}
