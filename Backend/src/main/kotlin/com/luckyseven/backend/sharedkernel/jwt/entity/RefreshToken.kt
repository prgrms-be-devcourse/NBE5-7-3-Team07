package com.luckyseven.backend.sharedkernel.jwt.entity

import com.luckyseven.backend.sharedkernel.jwt.util.JwtTokenizer
import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "refresh_token", indexes = [
   Index(name = "idx_refresh_token_value", columnList = "tokenValue"),
   Index(name = "idx_refresh_token_user_id", columnList = "userId")
])
class RefreshToken(
   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   val id: Long? = null,
   val userId: Long,
   @Column(length = 2000)
   val tokenValue: String,
   val issuedAt: Instant = Instant.now(),
   val expiresAt: Instant = Instant.now().plusMillis(JwtTokenizer.REFRESH_TOKEN_EXPIRE)
)