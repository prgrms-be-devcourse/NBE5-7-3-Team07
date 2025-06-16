package com.luckyseven.backend.domain.settlement.dao

import com.luckyseven.backend.domain.settlement.entity.Settlement
import jakarta.persistence.QueryHint
import org.springframework.data.jpa.repository.*
import org.springframework.stereotype.Repository
import java.util.stream.Stream

@Repository
interface SettlementRepository : JpaRepository<Settlement, Long>,
    JpaSpecificationExecutor<Settlement> {

    @EntityGraph(attributePaths = ["settler", "payer"])
    fun findWithSettlerAndPayerById(id: Long): Settlement?

    @EntityGraph(attributePaths = ["settler", "payer"])
    @Query("SELECT s From Settlement s join s.expense e where e.team.id = :teamId and s.isSettled = false")
    @QueryHints(QueryHint(name = "org.hibernate.fetchSize", value = "-2147483648"))
    fun findAllByTeamId(teamId: Long): Stream<Settlement>
}
