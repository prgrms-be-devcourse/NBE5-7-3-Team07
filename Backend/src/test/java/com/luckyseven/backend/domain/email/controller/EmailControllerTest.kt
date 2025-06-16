package com.luckyseven.backend.domain.email.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.luckyseven.backend.domain.email.dto.EmailRequest
import com.luckyseven.backend.domain.email.service.EmailService
import com.luckyseven.backend.domain.email.service.EmailVerificationService
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.eq
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.thymeleaf.context.Context

@SpringBootTest
@AutoConfigureMockMvc
class EmailControllerIntegrationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    // EmailController가 의존하는 서비스들을 Mockito 가짜 Bean으로 등록
    @MockitoBean
    private lateinit var verificationService: EmailVerificationService

    @MockitoBean
    private lateinit var emailService: EmailService

    @Test
    @DisplayName("이메일 인증 요청 API - 성공적으로 요청하고 201 Created를 응답한다")
    fun `requestEmail success`() {
        // given
        val request = EmailRequest("test@example.com")
        val requestJson = objectMapper.writeValueAsString(request)
        val generatedToken = "mock-verification-token"

        `when`(verificationService.generateAndSaveToken(request.email)).thenReturn(generatedToken)
        doNothing().`when`(emailService).sendTemplateEmail(eq(request.email), any(), any(), any<Context>())

        // when & then
        mockMvc.post("/api/email/request-email") {
            contentType = MediaType.APPLICATION_JSON
            content = requestJson
        }.andExpect {
            status { isCreated() }
            content { string("인증 메일을 발송했습니다.") }
        }.andDo { print() }
    }

    @Test
    @DisplayName("이메일 인증 확인 API - 유효한 토큰으로 인증에 성공하고 200 OK와 결과를 응답한다")
    fun `verifyEmail success`() {
        // given
        val validToken = "valid-token-123"
        val expectedEmail = "verified@example.com"

        `when`(verificationService.validateToken(validToken)).thenReturn(expectedEmail)
        doNothing().`when`(verificationService).deleteTokenByEmail(expectedEmail)

        // when & then
        mockMvc.get("/api/email/verify") {
            param("token", validToken)
        }.andExpect {
            status { isOk() }
            content { contentTypeCompatibleWith(MediaType.APPLICATION_JSON) }
            jsonPath("$.success") { value(true) }
            jsonPath("$.email") { value(expectedEmail) }
            jsonPath("$.message") { value("이메일 인증이 완료되었습니다.") }
        }.andDo { print() }
    }

    @Test
    @DisplayName("이메일 인증 확인 API - 유효하지 않은 토큰으로 인증에 실패하고 400 Bad Request를 응답한다")
    fun `verifyEmail failure`() {
        // given
        val invalidToken = "invalid-or-expired-token"

        `when`(verificationService.validateToken(invalidToken)).thenReturn(null)

        // when & then
        mockMvc.get("/api/email/verify") {
            param("token", invalidToken)
        }.andExpect {
            status { isBadRequest() }
            content { contentTypeCompatibleWith(MediaType.APPLICATION_JSON) }
            jsonPath("$.success") { value(false) }
            jsonPath("$.message") { value("유효하지 않은 토큰입니다.") }
        }.andDo { print() }
    }
}