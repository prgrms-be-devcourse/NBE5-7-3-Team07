package com.luckyseven.backend.domain.member.service

import com.luckyseven.backend.domain.member.dto.LoginMemberRequest
import com.luckyseven.backend.domain.member.dto.RegisterMemberRequest
import com.luckyseven.backend.domain.member.entity.Member
import com.luckyseven.backend.domain.member.repository.MemberRepository
import com.luckyseven.backend.domain.member.service.utill.MemberDetails
import com.luckyseven.backend.domain.member.validate.MemberValidator
import com.luckyseven.backend.sharedkernel.jwt.util.JwtTokenizer
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.DisplayName
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletResponse
import org.junit.jupiter.api.BeforeEach
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.crypto.password.PasswordEncoder

class MemberServiceTest {

    private lateinit var memberRepository: MemberRepository
    private lateinit var jwtTokenizer: JwtTokenizer
    private lateinit var authenticationManager: AuthenticationManager
    private lateinit var memberValidator: MemberValidator
    private lateinit var passwordEncoder: PasswordEncoder
    private lateinit var httpServletResponse: HttpServletResponse

    private lateinit var memberService: MemberService

    @BeforeEach
    fun setUp(){
        memberRepository = mockk()
        jwtTokenizer = mockk()
        authenticationManager = mockk()
        memberValidator = mockk()
        passwordEncoder = mockk(relaxed = true)
        httpServletResponse = mockk(relaxed = true)
        memberService = MemberService(
            memberRepository,
            jwtTokenizer,
            authenticationManager,
            memberValidator,
        )
    }

    @Test
    @DisplayName("회원가입 성공 - 올바른 정보로 요청 시 맴버 저장 후 이메일 반환")
    fun `회원가입 성공`(){

        //given
        val request = RegisterMemberRequest("test@example.com", "password1234", "password1234","testuser")
        val encodePassword = "encodedPassword123"

        /*
         * memberSlot -> Member 슬롯을 만들어서 잡는다.
         */

        val memberSlot = slot<Member>()

        every{memberValidator.registerRequestValidator(request)} returns Unit
        every{memberValidator.checkDuplicateEmail(request.email)} returns Unit
        every{memberValidator.checkDuplicateNickName(request.nickname)} returns Unit
        every{memberValidator.checkEqualsPassword(request.password, request.checkPassword)} returns Unit
        every{passwordEncoder.encode(request.password)} returns encodePassword
        every{memberRepository.save(any())} answers { firstArg() }

        //when
        val resultEmail = memberService.registerMember(request,passwordEncoder)

        //then
        /*
        * memberRepository.save 가 한번 호출되었는지 확인을 한다.
        * save의 메소드로 전달되었던 바로 그 객체를 찾아서 slot에 넣는다. -> memberService.registerMember 에서 save가 한번 호출된것을 의미
        * memberSlot에는 그럼 save에 넘어갓을때 담겨짐
        */
        verify(exactly = 1){memberRepository.save(capture(memberSlot))}
        /*
        * 이제 이 두개가 맞는지 확인을 한다.
        */
        val capturedMember = memberSlot.captured
        capturedMember.email shouldBe request.email
        capturedMember.password shouldBe encodePassword
        capturedMember.nickname shouldBe request.nickname

        verify(exactly = 1) { passwordEncoder.encode(request.password) }

        resultEmail shouldBe request.email
    }
    @Test
    @DisplayName("로그인 성공 - 올바른 정보로 요청 시 액세스 토큰 반환")
    fun `로그인 성공`(){
        //given
        val request = LoginMemberRequest("test@test.com","password123")
        val member = Member(1L,"test@test.com","encodedPassword","testuser")
        val memberDetails = MemberDetails(member);
        val authentication = mockk<Authentication>()
        val expectedAccessToken = "access-token-string"
        val memberDetailsSlot = slot<MemberDetails>()

        every{authenticationManager.authenticate(any<UsernamePasswordAuthenticationToken>())} returns authentication
        every{authentication.principal} returns memberDetails
        every{jwtTokenizer.reissueTokenPair(any(),capture(memberDetailsSlot))} returns expectedAccessToken

        //when
        val accessToken = memberService.login(request,httpServletResponse)

        //then
        //캡처된 MemberDetails의 내용이 올바른지 확인
        val capturedDetails = memberDetailsSlot.captured
        capturedDetails.getEmail() shouldBe request.email

        verify(exactly = 1){authenticationManager.authenticate(any<UsernamePasswordAuthenticationToken>())}
        accessToken shouldBe expectedAccessToken
    }
    @Test
    @DisplayName("로그아웃 성공 - 요청 시 토큰 비활성화 및 쿠키 만료")
    fun `로그아웃 성공`(){
        //given
        val refreshToken = "valid-refresh-token"
        //Cookie 객체를 캡처할 슬록
        val cookieSet = slot<Cookie>()

        every{jwtTokenizer.logoutRefreshToken(refreshToken)} returns Unit
        //addCookie에 전달되는 Cookie를 캡처
        every{httpServletResponse.addCookie(capture(cookieSet))} returns Unit

        //when
        memberService.logout(refreshToken,httpServletResponse)

        //then
        //캡처된 쿠키의 속성이 올바른지(만료되었는지)확인
        val capturedCookie = cookieSet.captured
        capturedCookie.name shouldBe "refreshToken"
        capturedCookie.maxAge shouldBe 0
        capturedCookie.value shouldBe null

        verify(exactly = 1){jwtTokenizer.logoutRefreshToken(refreshToken)}
    }
}