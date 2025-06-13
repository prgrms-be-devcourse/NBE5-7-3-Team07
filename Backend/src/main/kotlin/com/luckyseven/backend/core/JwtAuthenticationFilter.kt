package com.luckyseven.backend.core

import com.luckyseven.backend.domain.member.service.utill.MemberDetails
import com.luckyseven.backend.sharedkernel.exception.CustomLogicException
import com.luckyseven.backend.sharedkernel.exception.ExceptionCode
import com.luckyseven.backend.sharedkernel.jwt.util.JwtTokenizer
import io.jsonwebtoken.Claims
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.InternalAuthenticationServiceException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.util.PathMatcher
import org.springframework.util.StringUtils
import org.springframework.web.filter.OncePerRequestFilter
import java.io.IOException

@Component
class JwtAuthenticationFilter(
    private val jwtTokenizer: JwtTokenizer,
    private val authenticationEntryPoint: CustomAuthenticationEntryPoint,
    private val pathMatcher: PathMatcher
) : OncePerRequestFilter() {
    
    private val logger = LoggerFactory.getLogger(JwtAuthenticationFilter::class.java)
    
    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        val path = request.requestURI
        return path.startsWith("/api/users/") || path.startsWith("/api/email/")
    }
    
    @Throws(ServletException::class, IOException::class)
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val accessToken = resolveToken(request)
        
        try {
            if (StringUtils.hasText(accessToken)) {
                val claims = jwtTokenizer.parseAccessToken(accessToken!!)
                setAuthenticationToContext(claims)
                logger.info("[JwtAuthenticationFilter] doFilterInternal claims subject = ${claims.subject}")
            } else {
                throw CustomLogicException(ExceptionCode.JWT_TOKEN_NOT_FOUND)
            }
        } catch (ex: Exception) {
            SecurityContextHolder.clearContext()
            val authEx = if (ex is AuthenticationException) {
                ex
            } else {
                InternalAuthenticationServiceException(
                    ex.message ?: "Authentication failed"
                )
            }
            authenticationEntryPoint.commence(request, response, authEx)
            return
        }
        
        filterChain.doFilter(request, response)
    }
    
    private fun setAuthenticationToContext(claims: Claims) {
        val memberId = claims.subject.toLong()
        val email = claims["email"].toString()
        val nickName = claims["nickname"].toString()
        
        val userDetails = MemberDetails(memberId, " ", email, nickName)
        
        logger.info("userDetails - memberId: $memberId")
        logger.info("userDetails - email: $email")
        logger.info("userDetails - nickName: $nickName")
        
        val authentication: Authentication = UsernamePasswordAuthenticationToken(
            userDetails, null, userDetails.authorities
        )
        SecurityContextHolder.getContext().authentication = authentication
    }
    
    private fun resolveToken(request: HttpServletRequest): String? {
        val bearerToken = request.getHeader("Authorization")
        
        return if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            bearerToken.substring(7)
        } else {
            null
        }
    }
} 