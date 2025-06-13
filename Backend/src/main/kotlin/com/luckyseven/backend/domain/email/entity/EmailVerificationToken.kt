package com.luckyseven.backend.domain.email.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(
    name = "email_verification_tokens",
    indexes = [Index(columnList = "token", unique = true)]
)
 class EmailVerificationToken(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    @Column(nullable = false, unique = true, length = 36)
    val token: String,
    
    @Column(nullable = false)
    val email: String,
    
    @Column(nullable = false)
    val expireTime: LocalDateTime
) 