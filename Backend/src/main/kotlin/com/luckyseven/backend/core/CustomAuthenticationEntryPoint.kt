package com.luckyseven.backend.core

import com.fasterxml.jackson.databind.ObjectMapper
import com.luckyseven.backend.sharedkernel.exception.CustomLogicException
import com.luckyseven.backend.sharedkernel.exception.ErrorResponse
import com.luckyseven.backend.sharedkernel.exception.ExceptionCode
import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.stereotype.Component
import java.io.IOException

@Component
class CustomAuthenticationEntryPoint(
    private val objectMapper: ObjectMapper
) : AuthenticationEntryPoint {
    
    private val logger = LoggerFactory.getLogger(CustomAuthenticationEntryPoint::class.java)
    
    // TODO : JWT.parse에 있는 것과 중복로직이 아닌가?
    @Throws(IOException::class, ServletException::class)
    override fun commence(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authException: AuthenticationException
    ) {
        logger.info("authException.getMessage() == {}", authException.message)
        
        val ex = CustomLogicException(
            ExceptionCode.AUTHENTICATION_FAILED,
            authException.message
        )
        
        val error = ErrorResponse.from(ex)
        response.contentType = "application/json;charset=UTF-8"
        response.status = 400 // BAD_REQUEST
        
        response.writer.use { writer ->
            writer.write(objectMapper.writeValueAsString(error))
        }
    }
} 