package com.luckyseven.backend.domain.member.validate

import com.luckyseven.backend.domain.member.dto.RegisterMemberRequest
import com.luckyseven.backend.domain.member.repository.MemberRepository
import com.luckyseven.backend.sharedkernel.exception.CustomLogicException
import com.luckyseven.backend.sharedkernel.exception.ExceptionCode
import jakarta.validation.ConstraintViolation
import jakarta.validation.Validator
import org.springframework.stereotype.Component

@Component
class MemberValidator(
    private val memberRepository: MemberRepository,
    private val validator: Validator
) {
    
    fun registerRequestValidator(req: RegisterMemberRequest) {
        val violations: Set<ConstraintViolation<RegisterMemberRequest>> = validator.validate(req)
        if (violations.isNotEmpty()) {
            // 첫 번째 위반만 처리
            val v = violations.iterator().next()
            val field = v.propertyPath.toString()
            val message = v.message
            
            when (field) {
                "email" -> throw CustomLogicException(
                    ExceptionCode.INVALID_EMAIL_FORMAT, message
                )
                "password" -> throw CustomLogicException(
                    ExceptionCode.INVALID_PASSWORD_FORMAT, message
                )
                "checkPassword" -> throw CustomLogicException(
                    ExceptionCode.INVALID_CHECKPASSWORD_FORMAT, message
                )
                else -> throw CustomLogicException(
                    ExceptionCode.BAD_REQUEST, message
                )
            }
        }
    }
    
    fun checkEqualsPassword(password: String, checkPassword: String) {
        if (password != checkPassword) {
            throw CustomLogicException(ExceptionCode.MEMBER_PASSWORD_MISMATCH)
        }
    }
    
    fun checkDuplicateEmail(email: String) {
        memberRepository.findByEmail(email)?.let{
            foundMember -> throw CustomLogicException(ExceptionCode.MEMBER_EMAIL_DUPLICATE, foundMember.email)
        }
    }
    
    fun checkDuplicateNickName(nickname: String) {
        memberRepository.findByNickname(nickname)?.let{
            foundMember -> throw CustomLogicException(ExceptionCode.MEMBER_NICKNAME_DUPLICATE, foundMember.nickname)
        }
    }
} 