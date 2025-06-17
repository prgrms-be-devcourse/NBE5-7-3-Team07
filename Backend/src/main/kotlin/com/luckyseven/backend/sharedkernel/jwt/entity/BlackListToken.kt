package com.luckyseven.backend.sharedkernel.jwt.entity

import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "black_list_token", indexes = [
   Index(name = "idx_blacklist_token_value", columnList = "tokenValue"),
   Index(name = "idx_blacklist_expiration_time", columnList = "expirationTime")
])
 class BlackListToken(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    val tokenValue: String,
    
    val expirationTime: Instant
) 