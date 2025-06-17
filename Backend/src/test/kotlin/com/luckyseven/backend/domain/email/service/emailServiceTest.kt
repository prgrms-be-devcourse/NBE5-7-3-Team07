package com.luckyseven.backend.domain.email.service

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import jakarta.mail.internet.MimeMessage
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.mail.javamail.JavaMailSender
import org.thymeleaf.TemplateEngine
import org.thymeleaf.context.Context

class EmailServiceTest {

    // 의존성들을 Mock 객체로 선언
    private lateinit var javaMailSender: JavaMailSender
    private lateinit var templateEngine: TemplateEngine

    // 테스트 대상
    private lateinit var emailService: EmailService


    private lateinit var mimeMessage: MimeMessage

    @BeforeEach
    fun setUp() {

        javaMailSender = mockk()
        templateEngine = mockk()
        mimeMessage = mockk(relaxed = true)

        emailService = EmailService(javaMailSender, templateEngine)
    }

    @Test
    @DisplayName("템플릿 이메일 발송에 성공한다")
    fun `sendTemplateEmail success`() {
        // given
        val email = "test@example.com"
        val subject = "테스트 이메일 제목"
        val templateName = "test-template"
        val context = mockk<Context>()
        val htmlContent = "<html><body><h1>테스트</h1></body></html>"


        every { javaMailSender.createMimeMessage() } returns mimeMessage


        every { templateEngine.process(templateName, context) } returns htmlContent


        every { javaMailSender.send(mimeMessage) } just runs

        // when
        emailService.sendTemplateEmail(email, subject, templateName, context)

        // then

        verify(exactly = 1) { javaMailSender.createMimeMessage() }
        verify(exactly = 1) { templateEngine.process(eq(templateName), eq(context)) }
        verify(exactly = 1) { javaMailSender.send(mimeMessage) }
    }

    @Test
    @DisplayName("이메일 발송 중 예외가 발생하면, 해당 예외를 다시 던진다")
    fun `sendTemplateEmail throws exception on mail send failure`() {
        // given
        val email = "test@example.com"
        val subject = "테스트 이메일 제목"
        val templateName = "test-template"
        val context = mockk<Context>()
        val htmlContent = "<html><body><h1>테스트</h1></body></html>"
        val mailException = RuntimeException("Mail server is down")

        every { javaMailSender.createMimeMessage() } returns mimeMessage
        every { templateEngine.process(templateName, context) } returns htmlContent


        every { javaMailSender.send(mimeMessage) } throws mailException

        // when & then
        val thrownException = assertThrows<RuntimeException> {
            emailService.sendTemplateEmail(email, subject, templateName, context)
        }

        thrownException shouldBe mailException
    }
}