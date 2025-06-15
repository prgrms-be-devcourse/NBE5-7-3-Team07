package com.luckyseven.backend.domain.email.service
import com.luckyseven.backend.domain.email.entity.EmailVerificationToken
import com.luckyseven.backend.domain.email.repository.EmailVerificationTokenRepository
import io.kotest.matchers.shouldBe
import io.mockk.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.*

class EmailVerificationServiceTest {

    private lateinit var emailVerificationTokenRepository: EmailVerificationTokenRepository

    // 테스트 대상
    private lateinit var emailVerificationService: EmailVerificationService

    @BeforeEach
    fun setUp() {
        emailVerificationTokenRepository = mockk(relaxed = true)
        emailVerificationService = EmailVerificationService(emailVerificationTokenRepository)
    }

    @AfterEach
    fun tearDown() {
        unmockkStatic(UUID::class, LocalDateTime::class)
    }

    @Test
    @DisplayName("새로운 이메일 인증 토큰을 성공적으로 생성하고 저장한다")
    fun `generateAndSaveToken success`() {
        // given
        val email = "test@example.com"
        val fixedUuid = "fixed-uuid-1234-5678"
        val fixedNow = LocalDateTime.of(2025, 6, 16, 10, 0, 0)

        mockkStatic(UUID::class)
        every { UUID.randomUUID().toString() } returns fixedUuid
        mockkStatic(LocalDateTime::class)
        every { LocalDateTime.now() } returns fixedNow

        val tokenSlot = slot<EmailVerificationToken>()
        every { emailVerificationTokenRepository.save(capture(tokenSlot)) } returns mockk()

        // when
        val resultToken = emailVerificationService.generateAndSaveToken(email)

        // then
        resultToken shouldBe fixedUuid

        verify(exactly = 1) { emailVerificationTokenRepository.deleteByEmail(email) }
        verify(exactly = 1) { emailVerificationTokenRepository.save(any()) }

        val capturedToken = tokenSlot.captured
        capturedToken.email shouldBe email
        capturedToken.token shouldBe fixedUuid
        capturedToken.expireTime shouldBe fixedNow.plusHours(24)
    }

    @Test
    @DisplayName("유효하고 만료되지 않은 토큰 검증에 성공하고 이메일을 반환한다")
    fun `validateToken success`() {
        // given
        val token = "valid-token"
        val email = "test@example.com"
        val fixedNow = LocalDateTime.of(2025, 6, 16, 10, 0, 0)

        val emailToken = mockk<EmailVerificationToken>()
        every { emailToken.email } returns email
        every { emailToken.expireTime } returns fixedNow.plusMinutes(10) // 만료되지 않음

        every { emailVerificationTokenRepository.findByToken(token) } returns emailToken

        mockkStatic(LocalDateTime::class)
        every { LocalDateTime.now() } returns fixedNow

        // when
        val resultEmail = emailVerificationService.validateToken(token)

        // then
        resultEmail shouldBe email
        verify(exactly = 0) { emailVerificationTokenRepository.delete(any()) }
    }

    @Test
    @DisplayName("만료된 토큰 검증 시, 토큰을 삭제하고 null을 반환한다")
    fun `validateToken when token is expired`() {
        // given
        val token = "expired-token"
        val fixedNow = LocalDateTime.of(2025, 6, 16, 10, 0, 0)

        val emailToken = mockk<EmailVerificationToken>()
        every { emailToken.expireTime } returns fixedNow.minusMinutes(10) // 이미 만료됨

        every { emailVerificationTokenRepository.findByToken(token) } returns emailToken

        mockkStatic(LocalDateTime::class)
        every { LocalDateTime.now() } returns fixedNow

        // when
        val result = emailVerificationService.validateToken(token)

        // then
        result shouldBe null
        verify(exactly = 1) { emailVerificationTokenRepository.delete(emailToken) }
    }

    @Test
    @DisplayName("존재하지 않는 토큰 검증 시 null을 반환한다")
    fun `validateToken when token does not exist`() {
        // given
        val token = "non-existent-token"
        every { emailVerificationTokenRepository.findByToken(token) } returns null

        // when
        val result = emailVerificationService.validateToken(token)

        // then
        result shouldBe null
    }

    @Test
    @DisplayName("이메일로 토큰 삭제를 성공적으로 요청한다")
    fun `deleteTokenByEmail success`() {
        // given
        val email = "test@example.com"
        every { emailVerificationTokenRepository.deleteByEmail(email) } just runs

        // when
        emailVerificationService.deleteTokenByEmail(email)

        // then
        verify(exactly = 1) { emailVerificationTokenRepository.deleteByEmail(email) }
    }
}