package com.luckyseven.backend.domain.email.mapper;

import com.luckyseven.backend.domain.email.entity.EmailVerificationToken;
import java.time.LocalDateTime;
import org.springframework.stereotype.Component;

@Component
public class EmailVerificationMapper {
  public EmailVerificationToken makeEntity(String email , String token , LocalDateTime expirationDate){
    return EmailVerificationToken.builder()
        .email(email)
        .token(token)
        .expireTime(expirationDate)
        .build();
  }
}

