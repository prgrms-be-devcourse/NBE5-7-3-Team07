package com.luckyseven.backend.domain.email.repository

import com.luckyseven.backend.domain.email.entity.EmailVerificationToken
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface EmailVerificationTokenRepository : JpaRepository<EmailVerificationToken, Long> {
    
    fun findByToken(token: String): Optional<EmailVerificationToken>
    
    fun deleteByEmail(email: String)
} 