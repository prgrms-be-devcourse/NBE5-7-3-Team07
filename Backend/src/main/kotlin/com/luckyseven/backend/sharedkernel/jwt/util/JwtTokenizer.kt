package com.luckyseven.backend.sharedkernel.jwt.util

import com.luckyseven.backend.domain.member.service.CustomMemberDetailsService
import com.luckyseven.backend.domain.member.service.utill.MemberDetails
import com.luckyseven.backend.sharedkernel.exception.CustomLogicException
import com.luckyseven.backend.sharedkernel.exception.ExceptionCode
import com.luckyseven.backend.sharedkernel.jwt.entity.BlackListToken
import com.luckyseven.backend.sharedkernel.jwt.entity.RefreshToken
import com.luckyseven.backend.sharedkernel.jwt.repository.BlackListTokenRepository
import com.luckyseven.backend.sharedkernel.jwt.repository.RefreshTokenRepository
import io.jsonwebtoken.*
import io.jsonwebtoken.security.Keys
import jakarta.servlet.http.HttpServletResponse
import jakarta.transaction.Transactional
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseCookie
import org.springframework.stereotype.Component
import java.security.Key
import java.util.*
import javax.crypto.SecretKey

@Component
@Transactional
class JwtTokenizer(
    private val refreshTokenRepository: RefreshTokenRepository,
    private val blackListTokenRepository: BlackListTokenRepository,
    private val customMemberDetailsService: CustomMemberDetailsService,
    @Value("213948109238490182309481asdkasdfkajsdlf19023840921") accessSecret: String,
    @Value("213948109238490182309asdfasdfasdf4819023840921") refreshSecret: String
) {
    
    private val logger = LoggerFactory.getLogger(JwtTokenizer::class.java)
    private val accessSecret: ByteArray = Base64.getDecoder().decode(accessSecret)
    private val refreshSecret: ByteArray = Base64.getDecoder().decode(refreshSecret)
    
    companion object {
        const val ACCESS_TOKEN_EXPIRE = 24 * 60 * 60 * 1000L // 1일
        const val REFRESH_TOKEN_EXPIRE = 7 * 24 * 60 * 60 * 1000L // 1주일
    }
    
    fun reissueTokenPair(response: HttpServletResponse, memberDetails: MemberDetails): String {
        val accessToken = createToken(
            memberDetails,
            ACCESS_TOKEN_EXPIRE,
            getSigningKey(accessSecret)
        )
        
        val refreshToken = createToken(
            memberDetails,
            REFRESH_TOKEN_EXPIRE,
            getSigningKey(refreshSecret)
        )
        
        val refreshTokenEntity = refreshTokenRepository.findByUserId(memberDetails.id)
            .orElse(
                RefreshToken(
                    userId = memberDetails.id,
                    tokenValue = refreshToken
                )
            )
        
        refreshTokenRepository.save(refreshTokenEntity)
        addRefreshToken(response, refreshToken, REFRESH_TOKEN_EXPIRE)
        
        return accessToken
    }
    
    private fun createToken(userDetails: MemberDetails, expire: Long, signingKey: Key): String {
        return Jwts.builder()
            .subject(userDetails.id.toString())
            .claim("email", userDetails.getEmail())
            .claim("nickname", userDetails.username)
            .issuedAt(Date())
            .expiration(Date(Date().time + expire))
            .signWith(signingKey)
            .compact()
    }
    
    private fun addRefreshToken(
        response: HttpServletResponse,
        tokenValue: String,
        expirationTime: Long
    ) {
        val refreshToken = ResponseCookie.from("refreshToken", tokenValue)
            .httpOnly(true)
            .secure(true)
            .sameSite("Lax")
            .path("/")
            .maxAge(expirationTime)
            .domain("localhost")
            .build()
        
        logger.info("Setting Cookie - Name: {}, Value: {}", refreshToken.name, refreshToken.value)
        response.addHeader("Set-Cookie", refreshToken.toString())
    }
    
    fun parseAccessToken(accessToken: String): Claims {
        return parseToken(accessToken, accessSecret)
    }
    
    fun parseRefreshToken(refreshToken: String): Claims {
        return parseToken(refreshToken, refreshSecret)
    }
    
    fun parseToken(token: String, signingKey: ByteArray): Claims {
        return try {
            val parsed = Jwts.parser()
                .verifyWith(getSigningKey(signingKey))
                .build()
                .parseSignedClaims(token)
            
            parsed.payload
        } catch (e: ExpiredJwtException) {
            throw CustomLogicException(ExceptionCode.JWT_EXPIRED_TOKEN, e.message)
        } catch (e: Exception) {
            when (e) {
                is UnsupportedJwtException,
                is MalformedJwtException,
                is SecurityException,
                is IllegalArgumentException -> {
                    throw CustomLogicException(ExceptionCode.JWT_INVALID_TOKEN, e.message)
                }
                else -> throw e
            }
        }
    }
    
    private fun getSigningKey(signingKey: ByteArray): SecretKey {
        return Keys.hmacShaKeyFor(signingKey)
    }
    
    @Transactional
    fun validateRefreshToken(refreshToken: String, response: HttpServletResponse): String {
        if (blackListTokenRepository.existsByTokenValue(refreshToken)) {
            throw CustomLogicException(ExceptionCode.JWT_BLACKLISTED_TOKEN)
        }
        
        refreshTokenRepository.deleteByTokenValue(refreshToken)
        val claims = parseRefreshToken(refreshToken)
        val memberId = claims.subject.toLong()
        val user = customMemberDetailsService.loadUserById(memberId)
        
        blackListTokenRepository.save(
            BlackListToken(
                tokenValue = refreshToken,
                expirationTime = claims.expiration.toInstant()
            )
        )
        
        return reissueTokenPair(response, user)
    }
    
    fun logoutRefreshToken(refreshToken: String) {
        blackListTokenRepository.save(
            BlackListToken(
                tokenValue = refreshToken,
                expirationTime = parseRefreshToken(refreshToken).expiration.toInstant()
            )
        )
        refreshTokenRepository.deleteByTokenValue(refreshToken)
    }
} 