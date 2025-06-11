package com.luckyseven.backend.sharedkernel.jwt.repository;

import com.luckyseven.backend.sharedkernel.jwt.entity.RefreshToken;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken,Long> {


  Optional<RefreshToken> findByUserId(Long id);


  void deleteByTokenValue(String refreshToken);
}
