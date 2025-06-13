package com.luckyseven.backend.domain.email.repository

import com.luckyseven.backend.domain.email.entity.EmailVerificationToken
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface EmailVerificationTokenRepository : JpaRepository<EmailVerificationToken, Long> {
    fun findByToken(token: String): EmailVerificationToken?
    
    fun deleteByEmail(email: String)
} 