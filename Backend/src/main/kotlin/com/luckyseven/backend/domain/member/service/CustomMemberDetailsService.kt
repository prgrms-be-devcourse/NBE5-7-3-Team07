package com.luckyseven.backend.domain.member.service

import com.luckyseven.backend.domain.member.service.utill.MemberDetails
import com.luckyseven.backend.domain.member.entity.Member
import com.luckyseven.backend.domain.member.repository.MemberRepository
import com.luckyseven.backend.sharedkernel.exception.CustomLogicException
import com.luckyseven.backend.sharedkernel.exception.ExceptionCode
import jakarta.transaction.Transactional
import org.slf4j.LoggerFactory
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
@Transactional
class CustomMemberDetailsService(
    private val memberRepository: MemberRepository
) : UserDetailsService {
    
    private val logger = LoggerFactory.getLogger(CustomMemberDetailsService::class.java)
    
    @Throws(UsernameNotFoundException::class)
    override fun loadUserByUsername(email: String): MemberDetails {
        val member = memberRepository.findByEmail(email)
            .orElseThrow { CustomLogicException(ExceptionCode.MEMBER_EMAIL_NOTFOUND, email) }
        return MemberDetails(member)
    }
    
    fun loadUserById(id: Long): MemberDetails {
        val member = memberRepository.findById(id)
            .orElseThrow { CustomLogicException(ExceptionCode.MEMBER_ID_NOTFOUND, id) }
        return MemberDetails(member)
    }
} 