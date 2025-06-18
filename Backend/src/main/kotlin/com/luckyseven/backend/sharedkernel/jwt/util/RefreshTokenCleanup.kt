package com.luckyseven.backend.sharedkernel.jwt.util

import com.luckyseven.backend.sharedkernel.jwt.entity.BlackListToken
import com.luckyseven.backend.sharedkernel.jwt.repository.BlackListTokenRepository
import com.luckyseven.backend.sharedkernel.jwt.repository.RefreshTokenRepository
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class RefreshTokenCleanup(
    private val refreshRepo: RefreshTokenRepository,
    private val blackListRepo: BlackListTokenRepository
) {
    @Scheduled(cron = "0 0 * * * *") // 매시 정각
    fun cleanUpExpired() {
        val now = Instant.now()
        refreshRepo.findAllByExpiresAtBefore(now).forEach { expiredToken ->
            blackListRepo.save(
                BlackListToken(
                    tokenValue    = expiredToken.tokenValue,
                    expirationTime = expiredToken.expiresAt
                )
            )
            refreshRepo.delete(expiredToken)
        }
    }
}
