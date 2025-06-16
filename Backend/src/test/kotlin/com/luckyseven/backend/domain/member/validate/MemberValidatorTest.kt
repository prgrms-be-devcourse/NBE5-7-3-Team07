package com.luckyseven.backend.domain.member.validate

import com.luckyseven.backend.domain.member.dto.RegisterMemberRequest
import com.luckyseven.backend.domain.member.entity.Member
import com.luckyseven.backend.domain.member.repository.MemberRepository
import com.luckyseven.backend.sharedkernel.exception.CustomLogicException
import com.luckyseven.backend.sharedkernel.exception.ExceptionCode
import io.kotest.matchers.shouldBe
import io.mockk.every

import io.mockk.mockk
import io.mockk.verify
import jakarta.validation.ConstraintViolation
import jakarta.validation.Path
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import jakarta.validation.Validator
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows


class MemberValidatorTest {

    private lateinit var memberRepository: MemberRepository
    private lateinit var validator: Validator
    private lateinit var memberValidator : MemberValidator

    @BeforeEach
    fun setUp(){
        memberRepository = mockk()
        validator = mockk()
        memberValidator = MemberValidator(memberRepository,validator)
    }

    @Test
    @DisplayName("회원가입 요청 DTO 유효성 검사 통과 시 예외가 발생하지 않는다.")
    fun `회원가입 Request 검증 성공`(){
        //given
        val request = RegisterMemberRequest("test@example.com", "password1234", "password1234","testuser")
        //validator.validate가 비어있는 Set을 반환하도록 설정
        every{ validator.validate(request) } returns emptySet()

        //when & then
        assertDoesNotThrow { memberValidator.registerRequestValidator(request) }

        //validator.validate가 정확히 한 번 호출되었는지 확인
        verify(exactly = 1){ validator.validate(request) }
    }

    @Test
    @DisplayName("회원가입 요청의 DTO의 이메일 형식이 유효하지 않으면 INVAILID_EMAIL_FROMAT 얘외 발생")
    fun `registerRequestValidator 이메일 형식 실패`(){
        val request = RegisterMemberRequest("invalid-email-format", "password1234", "password1234","testuser")
        val errorMessage = "올바르지 않은 이메일 형식입니다."

        //ConstraintViolation 무엇인가? -> 이 violation은 뭘 담는것인가?
        //ConstraintViolation 어디가 잘못되었는가? 하는 무언가 (검사결과?)
        // violation -> 가짜 인증정보 (잘못된 인증정보)
        val violation = mockk<ConstraintViolation<RegisterMemberRequest>>()
        // 가짜위치정보
        val propertyPath = mockk<Path>()
        //validator.validate가 위반 사항을 담은 Set을 반환하도록 설정
        every{validator.validate(request)} returns setOf(violation)
        //위반 사항의 필드 경로가 'email'을 반환하도록 설정
        every{violation.propertyPath} returns propertyPath
        every{propertyPath.toString()} returns "email"
        //위반 사항의 메세지를 설정
        every{violation.message} returns errorMessage

        //when & then
        val exception = assertThrows<CustomLogicException>{
            memberValidator.registerRequestValidator(request)
        }

        exception.exceptionCode shouldBe ExceptionCode.INVALID_EMAIL_FORMAT
        exception.message shouldBe errorMessage
    }
    @Test
    @DisplayName("비밀번호와 확인 비밀번호가 일치하면 예외가 발생하지 않는다.")
    fun `checkEqualsPassword 성공`(){
        //given
        val password = "password123"
        val checkPassword = "password123"
        //when & then
        assertDoesNotThrow { memberValidator.checkEqualsPassword(password,checkPassword) }
    }
    @Test
    @DisplayName("비밀번호와 확인 비밀번호가 일치하지 않으면 MEMBER_PASSWORD_MISMATCH 예외가 발생")
    fun `checkEqualsPassword 실패`(){
        //given
        val password = "password123"
        val checkPassword = "DIFFFERENT_password123"

        //when&then
        val exception = assertThrows<CustomLogicException>{
            memberValidator.checkEqualsPassword(password,checkPassword)
        }
        exception.exceptionCode shouldBe ExceptionCode.MEMBER_PASSWORD_MISMATCH
    }
    @Test
    @DisplayName("이메일이 중복되지 않으면 예외가 발생하지 않는다.")
    fun `checkDuplicateEmail 성공`(){
        //given
        val email = "new@example().com"
        //repository의 findByEmail이 null을 반환하도록 설정
        every{memberRepository.findByEmail(email)} returns null
        //when & then
        assertDoesNotThrow { memberValidator.checkDuplicateEmail(email) }
        verify(exactly = 1){memberRepository.findByEmail(email)}
    }
    @Test
    @DisplayName("이메일이 중복되면 MEMBER_EMAIL_DUPLICATE 예외가 발생한다")
    fun `checkDuplicateEmail 실패`(){
        //given
        val email = "duplicate@example.com"
        val dummyMember = Member(1L,email,"password","nickname")

        every{ memberRepository.findByEmail(email)} returns dummyMember

        val exception = assertThrows<CustomLogicException>{
            memberValidator.checkDuplicateEmail(email)
        }
        exception.exceptionCode shouldBe ExceptionCode.MEMBER_EMAIL_DUPLICATE
    }

    @Test
    @DisplayName("닉네임이 중복되지 않으면 예외가 발생하지 않는다.")
    fun `checkDuplicateNickName 성공`(){
        //given
        val nickname = "new_user"
        every{
            memberRepository.findByNickname(nickname)
        }returns null
        //when  & then
        assertDoesNotThrow{
            memberValidator.checkDuplicateNickName(nickname)
        }
        verify(exactly = 1){
            memberRepository.findByNickname(nickname)
        }
    }
    @Test
    @DisplayName("닉네임이 중복되면 MEMBER_NICKNAME_DUPLICATE 예외가 발생한다.")
    fun `checkDuplicateNickName 실패`(){
        //given
        val nickname = "duplicate_user"
        val dummyMember = Member(1L,"email@example","password",nickname)
        every{memberRepository.findByNickname(nickname)} returns dummyMember

        // when & then
        val exception = assertThrows<CustomLogicException> {
            memberValidator.checkDuplicateNickName(nickname)
        }
        exception.exceptionCode shouldBe ExceptionCode.MEMBER_NICKNAME_DUPLICATE
    }

}