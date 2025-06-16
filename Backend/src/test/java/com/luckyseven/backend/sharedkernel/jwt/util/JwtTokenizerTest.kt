import com.luckyseven.backend.domain.member.entity.Member
import com.luckyseven.backend.domain.member.service.CustomMemberDetailsService
import com.luckyseven.backend.domain.member.service.utill.MemberDetails
import com.luckyseven.backend.sharedkernel.exception.CustomLogicException
import com.luckyseven.backend.sharedkernel.exception.ExceptionCode
import com.luckyseven.backend.sharedkernel.jwt.entity.BlackListToken
import com.luckyseven.backend.sharedkernel.jwt.entity.RefreshToken
import com.luckyseven.backend.sharedkernel.jwt.repository.BlackListTokenRepository
import com.luckyseven.backend.sharedkernel.jwt.repository.RefreshTokenRepository
import com.luckyseven.backend.sharedkernel.jwt.util.JwtTokenizer

import io.jsonwebtoken.*
import io.jsonwebtoken.security.Keys
import io.kotest.matchers.shouldBe
import io.mockk.*
import jakarta.servlet.http.HttpServletResponse
import org.junit.jupiter.api.*
import java.util.*
import javax.crypto.SecretKey

class JwtTokenizerTest {

    // 의존성 Mock 객체들을 lateinit var로 선언
    private lateinit var refreshTokenRepository: RefreshTokenRepository
    private lateinit var blackListTokenRepository: BlackListTokenRepository
    private lateinit var customMemberDetailsService: CustomMemberDetailsService
    private lateinit var httpServletResponse: HttpServletResponse
    private lateinit var memberDetails: MemberDetails


    private lateinit var jwtTokenizer: JwtTokenizer


    private val testAccessSecret = "a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2"
    private val testRefreshSecret = "f6e5d4c3b2a1f6e5d4c3b2a1f6e5d4c3b2a1f6e5d4c3b2a1f6e5d4c3b2a1f6e5"

    @BeforeEach
    fun setUp() {
        refreshTokenRepository = mockk(relaxed = true)
        blackListTokenRepository = mockk(relaxed = true)
        customMemberDetailsService = mockk(relaxed = true)
        httpServletResponse = mockk(relaxed = true)

        val mockMember = mockk<Member>()
        every { mockMember.id } returns 1L
        every { mockMember.email } returns "test@test.com"
        every { mockMember.nickname } returns "testuser"
        every { mockMember.password } returns "encodedPassword"

        memberDetails = MemberDetails(mockMember)

        mockkStatic(Jwts::class)
        mockkStatic(Keys::class)

        jwtTokenizer = JwtTokenizer(
            refreshTokenRepository,
            blackListTokenRepository,
            customMemberDetailsService,
            testAccessSecret,
            testRefreshSecret
        )
    }

    @AfterEach
    fun tearDown() {
        unmockkStatic(Jwts::class)
        unmockkStatic(Keys::class)
    }


    @Test
    @DisplayName("토큰 재발급 성공 - 새 사용자의 경우 새 리프레시 토큰을 생성하고 저장한다")
    fun `reissueTokenPair for new user`() {
        // given
        val expectedAccessToken = "mocked-access-token"
        val expectedRefreshToken = "mocked-refresh-token"

        every {
            Jwts.builder().subject(any()).claim(any(), any()).claim(any(), any()).issuedAt(any()).expiration(any()).signWith(any<SecretKey>()).compact()
        } returns expectedAccessToken andThen expectedRefreshToken

        every { refreshTokenRepository.findByUserId(1L) } returns null

        val refreshTokenSlot = slot<RefreshToken>()
        every { refreshTokenRepository.save(capture(refreshTokenSlot)) } answers { firstArg() }

        // when
        val actualAccessToken = jwtTokenizer.reissueTokenPair(httpServletResponse, memberDetails)

        // then
        actualAccessToken shouldBe expectedAccessToken

        val capturedRefreshToken = refreshTokenSlot.captured
        capturedRefreshToken.userId shouldBe 1L
        capturedRefreshToken.tokenValue shouldBe expectedRefreshToken

        verify(exactly = 1) { httpServletResponse.addHeader(eq("Set-Cookie"), any()) }
    }
//AI 짱이다 짱
    @Test
    @DisplayName("토큰 재발급 성공 - 기존 사용자의 경우 이전 토큰을 삭제하고 새 토큰을 저장한다")
    fun `reissueTokenPair for existing user`() {
        // given
        val expectedAccessToken = "new-access-token"
        val expectedRefreshToken = "new-refresh-token"
        val oldToken = mockk<RefreshToken>(relaxed = true)

        every {
            Jwts.builder().subject(any()).claim(any(), any()).claim(any(), any()).issuedAt(any()).expiration(any()).signWith(any<SecretKey>()).compact()
        } returns expectedAccessToken andThen expectedRefreshToken

        // DB에 기존 토큰이 존재한다고 가정
        every { refreshTokenRepository.findByUserId(1L) } returns oldToken

        // save 메소드가 호출될 때, 입력받은 객체를 그대로 반환하도록 설정합니다.
        every { refreshTokenRepository.save(any()) } answers { firstArg() }

        // when
        jwtTokenizer.reissueTokenPair(httpServletResponse, memberDetails)

        // then
        verify(exactly = 1) { refreshTokenRepository.delete(oldToken) }
        verify(exactly = 1) { refreshTokenRepository.save(any()) }
    }

    @Test
    @DisplayName("유효한 액세스 토큰 파싱에 성공한다")
    fun `parseAccessToken success`() {
        // given
        val accessToken = "valid-access-token"
        val mockClaims = mockk<Claims>()
        val mockJws = mockk<Jws<Claims>>()

        every {
            Jwts.parser().verifyWith(any<SecretKey>()).build().parseSignedClaims(accessToken)
        } returns mockJws
        every { mockJws.payload } returns mockClaims

        // when
        val resultClaims = jwtTokenizer.parseAccessToken(accessToken)

        // then
        resultClaims shouldBe mockClaims
    }

    @Test
    @DisplayName("만료된 토큰 파싱 시 JWT_EXPIRED_TOKEN 예외를 던진다")
    fun `parseToken with expired token`() {
        // given
        val expiredToken = "expired-token"
        every {
            Jwts.parser().verifyWith(any<SecretKey>()).build().parseSignedClaims(expiredToken)
        } throws ExpiredJwtException(mockk(relaxed = true), mockk(relaxed = true), "expired")

        // when & then
        val exception = assertThrows<CustomLogicException> {
            jwtTokenizer.parseAccessToken(expiredToken)
        }
        exception.exceptionCode shouldBe ExceptionCode.JWT_EXPIRED_TOKEN
    }

    @Test
    @DisplayName("유효하지 않은 토큰 파싱 시 JWT_INVALID_TOKEN 예외를 던진다")
    fun `parseToken with invalid token`() {
        // given
        val invalidToken = "invalid-token"
        every {
            Jwts.parser().verifyWith(any<SecretKey>()).build().parseSignedClaims(invalidToken)
        } throws MalformedJwtException("invalid")

        // when & then
        val exception = assertThrows<CustomLogicException> {
            jwtTokenizer.parseAccessToken(invalidToken)
        }
        exception.exceptionCode shouldBe ExceptionCode.JWT_INVALID_TOKEN
    }

    @Test
    @DisplayName("유효한 리프레시 토큰 검증 시, 이전 토큰은 블랙리스트 처리하고 새 토큰을 발급한다")
    fun `validateRefreshToken success`() {
        // given
        val refreshToken = "valid-refresh-token"
        val newAccessToken = "new-access-token"
        val claims = mockk<Claims>()
        val mockJws = mockk<Jws<Claims>>()

        every { blackListTokenRepository.findByTokenValue(refreshToken) } returns null
        every { blackListTokenRepository.save(any()) } answers { firstArg() }
        every { refreshTokenRepository.save(any()) } answers { firstArg() }

        every {
            Jwts.parser().verifyWith(any<SecretKey>()).build().parseSignedClaims(refreshToken)
        } returns mockJws
        every { mockJws.payload } returns claims
        every { claims.subject } returns "1"
        every { claims.expiration } returns Date(System.currentTimeMillis() + 10000)

        every { customMemberDetailsService.loadUserById(1L) } returns memberDetails

        every {
            Jwts.builder().subject(any()).claim(any(), any()).claim(any(), any()).issuedAt(any()).expiration(any()).signWith(any<SecretKey>()).compact()
        } returns newAccessToken andThen "new-refresh-token"

        // when
        val resultAccessToken = jwtTokenizer.validateRefreshToken(refreshToken, httpServletResponse)

        // then
        resultAccessToken shouldBe newAccessToken
        verify(exactly = 1) { refreshTokenRepository.deleteByTokenValue(refreshToken) }
        verify(exactly = 1) { blackListTokenRepository.save(any()) }
    }

    @Test
    @DisplayName("블랙리스트에 있는 리프레시 토큰 검증 시 JWT_BLACKLISTED_TOKEN 예외를 던진다")
    fun `validateRefreshToken with blacklisted token`() {
        // given
        val blacklistedToken = "blacklisted-token"
        every { blackListTokenRepository.findByTokenValue(blacklistedToken) } returns mockk<BlackListToken>()

        // when & then
        val exception = assertThrows<CustomLogicException> {
            jwtTokenizer.validateRefreshToken(blacklistedToken, httpServletResponse)
        }
        exception.exceptionCode shouldBe ExceptionCode.JWT_BLACKLISTED_TOKEN
    }


    @Test
    @DisplayName("로그아웃 요청 시, 리프레시 토큰을 블랙리스트에 추가하고 DB에서 삭제한다")
    fun `logoutRefreshToken success`() {
        // given
        val refreshToken = "token-to-logout"
        val claims = mockk<Claims>()
        val blackListSlot = slot<BlackListToken>()
        val expirationTime = Date(System.currentTimeMillis() + 10000)
        val mockJws = mockk<Jws<Claims>>()

        every {
            Jwts.parser().verifyWith(any<SecretKey>()).build().parseSignedClaims(refreshToken)
        } returns mockJws
        every { mockJws.payload } returns claims
        every { claims.expiration } returns expirationTime

        every { blackListTokenRepository.save(capture(blackListSlot)) } returns mockk()

        // when
        jwtTokenizer.logoutRefreshToken(refreshToken)

        // then
        val capturedToken = blackListSlot.captured
        capturedToken.tokenValue shouldBe refreshToken
        capturedToken.expirationTime shouldBe expirationTime.toInstant()

        verify(exactly = 1) { refreshTokenRepository.deleteByTokenValue(refreshToken) }
    }
}


