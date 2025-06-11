package com.luckyseven.backend.sharedkernel.jwt.repository;

import com.luckyseven.backend.sharedkernel.jwt.entity.BlackListToken;
import jakarta.transaction.Transactional;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BlackListTokenRepository extends JpaRepository<BlackListToken,Long> {

 Boolean existsByTokenValue(String tokenValue);

 Optional<BlackListToken> findByTokenValue(String tokenValue);

}
