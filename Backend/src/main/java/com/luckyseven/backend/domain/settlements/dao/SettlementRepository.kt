package com.luckyseven.backend.domain.settlements.dao

import com.luckyseven.backend.domain.settlements.entity.Settlement
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.Specification
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.stereotype.Repository

@Repository
interface SettlementRepository : JpaRepository<Settlement, Long>,
    JpaSpecificationExecutor<Settlement?> {

    @EntityGraph(attributePaths = ["settler", "payer"])
    fun findWithSettlerAndPayerById(id: Long): Settlement?

    fun findAll(spec: Specification<Settlement>, page: Pageable): Page<Settlement>
}
