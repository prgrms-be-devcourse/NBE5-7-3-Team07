package com.luckyseven.backend.domain.settlements.dao;

import com.luckyseven.backend.domain.settlements.entity.Settlement;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface SettlementRepository extends JpaRepository<Settlement, Long>,
    JpaSpecificationExecutor<Settlement> {

  @EntityGraph(attributePaths = {"settler", "payer"})
  Optional<Settlement> findWithSettlerAndPayerById(Long id);


}
