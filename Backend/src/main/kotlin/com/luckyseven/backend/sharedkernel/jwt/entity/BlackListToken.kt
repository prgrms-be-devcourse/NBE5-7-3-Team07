package com.luckyseven.backend.sharedkernel.jwt.entity

import jakarta.persistence.*
import java.time.Instant

@Entity
data class BlackListToken(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    val tokenValue: String,
    
    val expirationTime: Instant
) 