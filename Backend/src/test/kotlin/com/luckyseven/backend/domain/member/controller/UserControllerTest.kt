package com.luckyseven.backend.domain.member.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.luckyseven.backend.domain.member.dto.LoginMemberRequest
import com.luckyseven.backend.domain.member.dto.RegisterMemberRequest
import com.luckyseven.backend.domain.member.service.MemberService
import com.luckyseven.backend.sharedkernel.exception.CustomLogicException
import com.luckyseven.backend.sharedkernel.exception.ExceptionCode
import jakarta.servlet.http.Cookie
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.Mockito.`when`

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post


@SpringBootTest
@AutoConfigureMockMvc
class MemberControllerIntegrationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockitoBean
    private lateinit var memberService: MemberService


    @Test
    @DisplayName("회원가입 API - 성공적으로 등록하고 생성된 이메일을 201 Created로 응답한다")
    fun `register member with valid request`() {
        // given
        val request = RegisterMemberRequest("test@example.com", "password123", "password123", "testuser")
        val requestJson = objectMapper.writeValueAsString(request)

        `when`(memberService.registerMember(any(), any())).thenReturn(request.email)

        // when & then
        mockMvc.post("/api/users/register") {
            contentType = MediaType.APPLICATION_JSON
            content = requestJson
        }.andExpect {
            status { isCreated() }
            content { string(request.email) }
        }.andDo { print() }
    }

    @Test
    @DisplayName("로그인 API - 성공적으로 로그인하고 Authorization 헤더에 토큰을 담아 200 OK로 응답한다")
    fun `login with valid credentials`() {
        // given
        val request = LoginMemberRequest("test@example.com", "password123")
        val requestJson = objectMapper.writeValueAsString(request)
        val accessToken = "mock-jwt-access-token"

        `when`(memberService.login(any(), any())).thenReturn(accessToken)

        // when & then
        mockMvc.post("/api/users/login") {
            contentType = MediaType.APPLICATION_JSON
            content = requestJson
        }.andExpect {
            status { isOk() }
            header { string("Authorization", "Bearer $accessToken") }
        }.andDo { print() }
    }

    @Test
    @DisplayName("이메일 중복 체크 API - 중복된 경우 409 Conflict를 응답한다")
    fun `checkEmail when duplicated`() {
        // given
        val email = "duplicate@example.com"

        `when`(memberService.checkDuplicateEmail(email))
            .thenThrow(CustomLogicException(ExceptionCode.MEMBER_EMAIL_DUPLICATE))

        // when & then
        mockMvc.post("/api/users/checkEmail") {
            param("email", email)
        }.andExpect {
            // 실제 애플리케이션의 @RestControllerAdvice가 예외를 409 코드로 처리
            status { isConflict() }
        }.andDo { print() }
    }

    @Test
    @DisplayName("로그아웃 API - 성공적으로 처리하고 204 No Content를 응답한다")
    fun `logout successfully`() {
        // given
        val refreshToken = "user-refresh-token"
        val cookie = Cookie("refreshToken", refreshToken)

        `when`(memberService.logout(any(), any())).then {}

        // when & then
        mockMvc.post("/api/users/logout") {
            cookie(cookie)
        }.andExpect {
            status { isNoContent() }
        }.andDo { print() }
    }
}