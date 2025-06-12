package com.luckyseven.backend.sharedkernel.jwt.repository

import com.luckyseven.backend.sharedkernel.jwt.entity.RefreshToken
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface RefreshTokenRepository : JpaRepository<RefreshToken, Long> {
    
    fun findByUserId(id: Long): RefreshToken?
    
    fun deleteByTokenValue(refreshToken: String)
} 