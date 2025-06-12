package com.luckyseven.backend.domain.member.repository

import com.luckyseven.backend.domain.member.entity.Member
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface MemberRepository : JpaRepository<Member, Long> {
    
    fun findByNickname(nickname: String): Optional<Member>
    
    fun findByEmail(email: String): Optional<Member>
} 