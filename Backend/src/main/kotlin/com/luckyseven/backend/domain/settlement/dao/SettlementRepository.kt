package com.luckyseven.backend.domain.settlement.dao

import com.luckyseven.backend.domain.settlement.entity.Settlement
import jakarta.persistence.QueryHint
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Query
import org.springframework.data.jpa.repository.QueryHints
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

    @Query(
        "SELECT s FROM Settlement s " +
                "WHERE s.expense.team.id = :teamId " +
                "AND ((s.payer.id = :from AND s.settler.id = :to) " +
                "OR (s.payer.id = :to AND s.settler.id = :from))"
    )
    fun findAssociatedNotSettled(teamId: Long, from: Long, to: Long): Stream<Settlement>

    fun findByExpenseIdIn(expenseIds: List<Long>): List<Settlement>
}
