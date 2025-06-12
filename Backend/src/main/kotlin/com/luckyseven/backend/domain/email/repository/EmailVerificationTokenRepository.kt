package com.luckyseven.backend.domain.email.repository

import com.luckyseven.backend.domain.email.entity.EmailVerificationToken
import org.springframework.data.jpa.repository.JpaRepository

interface EmailVerificationTokenRepository : JpaRepository<EmailVerificationToken, Long> {
    
    fun findByToken(token: String): EmailVerificationToken?
    
    fun findByEmail(email: String): EmailVerificationToken?
    
    fun deleteByEmail(email: String)
} 