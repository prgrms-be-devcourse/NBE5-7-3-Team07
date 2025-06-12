package com.luckyseven.backend.domain.member.repository

import com.luckyseven.backend.domain.member.entity.Member
import org.springframework.data.jpa.repository.JpaRepository

interface MemberRepository : JpaRepository<Member, Long> {
    
    fun findByNickname(nickname: String): Member?
    
    fun findByEmail(email: String): Member?
} 