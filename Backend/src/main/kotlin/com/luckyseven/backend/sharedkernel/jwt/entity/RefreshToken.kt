package com.luckyseven.backend.sharedkernel.jwt.entity

import jakarta.persistence.*

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
    
    val tokenValue: String
) 