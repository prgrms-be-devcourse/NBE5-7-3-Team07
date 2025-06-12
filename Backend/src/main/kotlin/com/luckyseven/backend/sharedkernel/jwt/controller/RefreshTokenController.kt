package com.luckyseven.backend.sharedkernel.jwt.controller

import com.luckyseven.backend.sharedkernel.jwt.util.JwtTokenizer
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CookieValue
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class RefreshTokenController(
    private val jwtTokenizer: JwtTokenizer
) {
    
    @PostMapping("/api/refresh")
    fun refreshToken(
        @CookieValue(name = "refreshToken") token: String,
        response: HttpServletResponse
    ): ResponseEntity<Void> {
        val accessToken = jwtTokenizer.validateRefreshToken(token, response)
        return ResponseEntity.ok()
            .header("Authorization", "Bearer $accessToken")
            .build()
    }
} 