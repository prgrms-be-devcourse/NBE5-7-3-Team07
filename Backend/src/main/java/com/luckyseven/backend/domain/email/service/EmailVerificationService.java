package com.luckyseven.backend.domain.email.service;

import com.luckyseven.backend.domain.email.entity.EmailVerificationToken;
import com.luckyseven.backend.domain.email.mapper.EmailVerificationMapper;
import com.luckyseven.backend.domain.email.repository.EmailVerificationTokenRepository;
import com.luckyseven.backend.domain.member.validate.MemberValidator;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailVerificationService {

  private final EmailVerificationTokenRepository tokenRepository;
  private final MemberValidator memberValidator;
  private final EmailVerificationMapper emailVerificationMapper;



  /**
   * 이메일로 인증 토큰 생성 후 DB에 저장
   *
   * @param email 인증 대상 이메일
   * @return 생성된 토큰(UUID)
   */
  @Transactional
  public String generateAndSaveToken(String email){
    tokenRepository.deleteByEmail(email);
    memberValidator.checkDuplicateEmail(email);
    String token = UUID.randomUUID().toString();
    LocalDateTime expiresAt = LocalDateTime.now().plus(1, ChronoUnit.HOURS);
    tokenRepository.save(emailVerificationMapper.makeEntity(email, token, expiresAt));
    return token;
  }


  /**
   * @param token  : token 검증할 토큰(UUID)
   * @return 유효 토큰일 경우 email , 아니라면 null
   */
  public String validateToken(String token){
    return tokenRepository.findByToken(token)
        .filter(t -> t.getExpireTime().isAfter(LocalDateTime.now()))
        .map(EmailVerificationToken::getEmail)
        .orElse(null);
  }


  /**
   * @param email 검증하고나서 검증 성공하면 토큰을 삭제
   */
  @Transactional
  public void deleteTokenByEmail(String email){
    tokenRepository.deleteByEmail(email);
  }

}
