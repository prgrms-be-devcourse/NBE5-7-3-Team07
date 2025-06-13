package com.luckyseven.backend.sharedkernel.jwt.repository

import com.luckyseven.backend.sharedkernel.jwt.entity.BlackListToken
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface BlackListTokenRepository : JpaRepository<BlackListToken, Long> {
    fun findByTokenValue(tokenValue: String): BlackListToken?
} 