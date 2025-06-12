package com.luckyseven.backend.sharedkernel.jwt.entity

import jakarta.persistence.*

@Entity
data class RefreshToken(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    val userId: Long,
    
    val tokenValue: String
) 