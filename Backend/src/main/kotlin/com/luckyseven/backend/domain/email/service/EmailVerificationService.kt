package com.luckyseven.backend.domain.email.service

import com.luckyseven.backend.domain.email.entity.EmailVerificationToken
import com.luckyseven.backend.domain.email.repository.EmailVerificationTokenRepository
import jakarta.transaction.Transactional
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.*

@Service
@Transactional
class EmailVerificationService(
    private val emailVerificationTokenRepository: EmailVerificationTokenRepository
) {
    
    private val logger = LoggerFactory.getLogger(EmailVerificationService::class.java)
    
    fun generateAndSaveToken(email: String): String {
        // 기존 토큰이 있다면 삭제
        emailVerificationTokenRepository.deleteByEmail(email)
        
        // 새 토큰 생성
        val token = UUID.randomUUID().toString()
        val expireTime = LocalDateTime.now().plusHours(24)
        
        val emailToken = EmailVerificationToken(
            token = token,
            email = email,
            expireTime = expireTime
        )
        emailVerificationTokenRepository.save(emailToken)
        
        logger.debug("이메일 인증 토큰 생성: email={}, token={}", email, token.substring(0, 8) + "...")
        return token
    }
    
    fun validateToken(token: String): String? {
        return try {
            val emailToken = emailVerificationTokenRepository.findByToken(token)
                ?: return null
            
            if (emailToken.expireTime.isBefore(LocalDateTime.now())) {
                emailVerificationTokenRepository.delete(emailToken)
                logger.warn("만료된 토큰: {}", token.substring(0, 8) + "...")
                return null
            }
            
            emailToken.email
        } catch (e: Exception) {
            logger.error("토큰 검증 중 오류: {}", token.substring(0, 8) + "...", e)
            null
        }
    }
    
    fun deleteTokenByEmail(email: String) {
        emailVerificationTokenRepository.deleteByEmail(email)
        logger.debug("이메일 토큰 삭제: {}", email)
    }
} 