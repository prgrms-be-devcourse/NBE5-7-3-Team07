package com.luckyseven.backend.domain.member.service

import com.luckyseven.backend.domain.member.dto.LoginMemberRequest
import com.luckyseven.backend.domain.member.dto.RegisterMemberRequest
import com.luckyseven.backend.domain.member.entity.Member
import com.luckyseven.backend.domain.member.repository.MemberRepository
import com.luckyseven.backend.domain.member.service.utill.MemberDetails
import com.luckyseven.backend.domain.member.validate.MemberValidator
import com.luckyseven.backend.sharedkernel.exception.CustomLogicException
import com.luckyseven.backend.sharedkernel.exception.ExceptionCode
import com.luckyseven.backend.sharedkernel.jwt.repository.BlackListTokenRepository
import com.luckyseven.backend.sharedkernel.jwt.util.JwtTokenizer
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletResponse
import jakarta.transaction.Transactional
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
@Transactional
class MemberService(
    private val memberRepository: MemberRepository,
    private val jwtTokenizer: JwtTokenizer,
    private val authenticationManager: AuthenticationManager,
    private val memberValidator: MemberValidator
) {
    
    private val logger = LoggerFactory.getLogger(MemberService::class.java)
    
    fun checkDuplicateNickName(nickname: String) {
        memberValidator.checkDuplicateNickName(nickname)
    }
    
    fun checkDuplicateEmail(email: String) {
        memberValidator.checkDuplicateEmail(email)
    }
    
    fun checkEqualsPassword(password: String, checkPassword: String) {
        memberValidator.checkEqualsPassword(password, checkPassword)
    }
    
    fun registerMember(req: RegisterMemberRequest, passwordEncoder: PasswordEncoder): String {
        memberValidator.registerRequestValidator(req)
        memberValidator.checkDuplicateEmail(req.email)
        memberValidator.checkDuplicateNickName(req.nickname)
        memberValidator.checkEqualsPassword(req.password, req.checkPassword)
        
        //TODO : {Mapper} : 설정
        val newMember = Member(
            email = req.email,
            password = passwordEncoder.encode(req.password),
            nickname = req.nickname
        )
        memberRepository.save(newMember)
        return req.email
    }
    
    fun logout(refreshToken: String, resp: HttpServletResponse) {
        jwtTokenizer.logoutRefreshToken(refreshToken)
        
        //TODO: 올바른 삭제 방법인가?
        val expired = Cookie("refreshToken", null)
        expired.path = "/"
        expired.maxAge = 0
        resp.addCookie(expired)
    }
    
    fun login(req: LoginMemberRequest, resp: HttpServletResponse): String {
        val token = UsernamePasswordAuthenticationToken(req.email, req.password)
        val auth: Authentication = authenticationManager.authenticate(token)
        val memberDetails = auth.principal as MemberDetails
        return jwtTokenizer.reissueTokenPair(resp, memberDetails)
    }
    
    fun findMemberOrThrow(id: Long): Member {
        return memberRepository.findById(id).orElseThrow {
            CustomLogicException(ExceptionCode.MEMBER_ID_NOTFOUND, id)
        }
    }
    
    fun findMembersByIds(ids: List<Long>): List<Member> {
        val members = memberRepository.findAllById(ids)
        if (members.size != ids.toSet().size) {
            throw CustomLogicException(ExceptionCode.MEMBER_ID_NOTFOUND)
        }
        return members
    }
} 