package com.luckyseven.backend.sharedkernel.jwt.util

import com.luckyseven.backend.sharedkernel.jwt.entity.BlackListToken
import com.luckyseven.backend.sharedkernel.jwt.entity.RefreshToken
import com.luckyseven.backend.sharedkernel.jwt.repository.BlackListTokenRepository
import com.luckyseven.backend.sharedkernel.jwt.repository.RefreshTokenRepository
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.Instant

class RefreshTokenCleanupTest {

    private lateinit var refreshRepository: RefreshTokenRepository
    private lateinit var blackListRepository: BlackListTokenRepository
    private lateinit var cleanup: RefreshTokenCleanup

    @BeforeEach
    fun setUp() {
        refreshRepository = mockk()
        blackListRepository = mockk()
        cleanup = RefreshTokenCleanup(refreshRepository, blackListRepository)
    }

    @Test
    @DisplayName("cleanUpExpired(): 만료된 리프레시 토큰을 블랙리스트로 이동 후 삭제")
    fun testCleanUpExpired() {
        // Given: 만료된 토큰 준비
        val pastInstant = Instant.now().minusSeconds(10)
        val expiredToken = RefreshToken(
            id = 1L,
            userId = 42L,
            tokenValue = "expired-token",
            issuedAt = pastInstant.minusSeconds(100),
            expiresAt = pastInstant
        )
        every { refreshRepository.findAllByExpiresAtBefore(any()) } returns listOf(expiredToken)
        // save는 받은 객체를 그대로 리턴하도록 스텁 설정
        every { blackListRepository.save(any()) } answers { firstArg<BlackListToken>() }
        // delete는 부작용만 실행
        justRun { refreshRepository.delete(expiredToken) }

        // When: 스케줄러 실행
        cleanup.cleanUpExpired()

        // Then: 블랙리스트 저장 호출 검증
        verify(exactly = 1) {
            blackListRepository.save(match { bl ->
                println("▶ Saved to blacklist: tokenValue=${bl.tokenValue}, expirationTime=${bl.expirationTime}")
                bl.tokenValue == expiredToken.tokenValue && bl.expirationTime == expiredToken.expiresAt
            })
        }
        // Then: 원본 토큰 삭제 호출 검증
        verify(exactly = 1) { refreshRepository.delete(expiredToken) }
    }
}
