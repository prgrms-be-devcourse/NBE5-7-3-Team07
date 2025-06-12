package com.luckyseven.backend.domain.email.repository;


import com.luckyseven.backend.domain.email.entity.EmailVerificationToken;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken,Long> {
  Optional<EmailVerificationToken> findByEmail(String email);

  Optional<EmailVerificationToken> findByToken(String token);

  void deleteByEmail(String email);
}
