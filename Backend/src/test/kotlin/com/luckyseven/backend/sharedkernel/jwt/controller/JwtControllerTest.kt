package com.luckyseven.backend.sharedkernel.jwt.controller

import com.luckyseven.backend.sharedkernel.jwt.util.JwtTokenizer
import jakarta.servlet.http.Cookie
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post

@SpringBootTest
@AutoConfigureMockMvc
class RefreshTokenControllerIntegrationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    // RefreshTokenController가 의존하는 JwtTokenizer를 Mockito 가짜 Bean으로 등록
    @MockitoBean
    private lateinit var jwtTokenizer: JwtTokenizer

    @Test
    @DisplayName("리프레시 API - 유효한 리프레시 토큰으로 새 액세스 토큰을 성공적으로 발급한다")
    fun `refreshToken successfully issues a new access token`() {
        // given
        val requestRefreshToken = "valid-user-refresh-token"
        val newAccessToken = "newly-issued-access-token"
        val requestCookie = Cookie("refreshToken", requestRefreshToken)

        `when`(jwtTokenizer.validateRefreshToken(eq(requestRefreshToken), any())).thenReturn(newAccessToken)

        // when & then
        mockMvc.post("/api/refresh") {
            cookie(requestCookie)
        }.andExpect {
            status { isOk() }
            header { string("Authorization", "Bearer $newAccessToken") }
        }.andDo {
            print()
        }
    }
}