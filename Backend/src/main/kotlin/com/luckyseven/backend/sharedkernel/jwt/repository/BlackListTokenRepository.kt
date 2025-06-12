package com.luckyseven.backend.sharedkernel.jwt.repository

import com.luckyseven.backend.sharedkernel.jwt.entity.BlackListToken
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface BlackListTokenRepository : JpaRepository<BlackListToken, Long> {
    
    fun existsByTokenValue(tokenValue: String): Boolean
    
    fun findByTokenValue(tokenValue: String): Optional<BlackListToken>
} 