package com.luckyseven.backend.sharedkernel.jwt.entity;


import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BlackListToken {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String tokenValue;

  private Instant expirationTime;

  @Builder
  public BlackListToken(String tokenValue, Instant expirationTime) {
    this.tokenValue = tokenValue;
    this.expirationTime = expirationTime;
  }


}
