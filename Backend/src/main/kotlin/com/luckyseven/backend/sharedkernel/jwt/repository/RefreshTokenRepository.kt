package com.luckyseven.backend.sharedkernel.jwt.repository

import com.luckyseven.backend.sharedkernel.jwt.entity.RefreshToken
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
interface RefreshTokenRepository : JpaRepository<RefreshToken, Long> {
    fun findByUserId(id: Long): RefreshToken?
    
    fun deleteByTokenValue(refreshToken: String)

    fun findAllByExpiresAtBefore(now: Instant): List<RefreshToken>
} 