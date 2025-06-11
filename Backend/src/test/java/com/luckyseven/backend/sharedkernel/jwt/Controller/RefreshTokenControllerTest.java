package com.luckyseven.backend.sharedkernel.jwt.Controller;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.luckyseven.backend.domain.member.dto.LoginMemberRequest;
import com.luckyseven.backend.domain.member.entity.Member;
import com.luckyseven.backend.domain.member.repository.MemberRepository;
import com.luckyseven.backend.sharedkernel.exception.CustomLogicException;
import com.luckyseven.backend.sharedkernel.exception.ExceptionCode;
import com.luckyseven.backend.sharedkernel.jwt.entity.BlackListToken;
import com.luckyseven.backend.sharedkernel.jwt.repository.BlackListTokenRepository;
import jakarta.servlet.http.Cookie;
import java.util.Arrays;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class RefreshTokenControllerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @Autowired private MemberRepository  memberRepository;
  @Autowired private PasswordEncoder passwordEncoder;
  @Autowired private BlackListTokenRepository blackListTokenRepository;

  @BeforeEach
  void setUp() {
    memberRepository.deleteAll();
  }

  @Test
  @DisplayName("POST /api/refresh")
  void login_then_refresh_success() throws Exception {
    String email = "test@test.com";
    String password = "test";
    Member member = Member.builder()
        .email(email)
        .password(passwordEncoder.encode(password))
        .nickname("test")
        .build();

    memberRepository.save(member);

    LoginMemberRequest loginRequest = new LoginMemberRequest(email, password);
    var loginResult = mockMvc.perform(
        post("/api/users/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(loginRequest)))
        .andExpect(status().isOk())
        .andReturn();

    String authHeader = loginResult.getResponse().getHeader(HttpHeaders.AUTHORIZATION);
    String accessToken = authHeader.substring("Bearer ".length());
    System.out.println("첫번쨰 accessToken 로그인  : " + accessToken);

    Cookie refreshTokenCookie = Arrays.stream(loginResult.getResponse().getCookies())
        .filter(cookie -> "refreshToken".equals(cookie.getName()))
        .findFirst()
        .orElseThrow(() -> new CustomLogicException(ExceptionCode.AUTHENTICATION_FAILED));
    String refreshToken = refreshTokenCookie.getValue();
    System.out.println("첫번쨰 refreshTokenCookie: " + refreshToken);

    var refreshTokenResult = mockMvc.perform(post("/refresh")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
        .cookie(new Cookie("refreshToken",refreshToken)))
        .andExpect(status().isOk())
        .andReturn();

    String newAuthHeader = refreshTokenResult.getResponse().getHeader(HttpHeaders.AUTHORIZATION);
    String newAccessToken = newAuthHeader.substring("Bearer ".length());
    System.out.println("두번째 AccessToken " + newAccessToken);
    Cookie newRfreshTokenCookie = (Cookie) Arrays.stream(refreshTokenResult.getResponse().getCookies())
        .filter(cookie -> "refreshToken".equals(cookie.getName()))
        .findFirst()
        .orElseThrow(() -> new CustomLogicException(ExceptionCode.AUTHENTICATION_FAILED));
    String newRefreshToken = newRfreshTokenCookie.getValue();
    System.out.println("두번쨰  refreshTokenCookie: " + newRefreshToken);


    System.out.println("첫번째 RefreshToken이 블랙리스트 안에 들어갔으면 True == " +  blackListTokenRepository.existsByTokenValue(refreshToken));
    System.out.println("두번째 RefreshToken은 아직 들어가지 않았으므로 false == "+ blackListTokenRepository.existsByTokenValue(newAccessToken));

    Optional<BlackListToken> findByTokenValue = blackListTokenRepository.findByTokenValue(refreshToken);
    System.out.println("blackList안에 들어간 id == "+findByTokenValue.get().getId());
    System.out.println("blackList안에 들어간 value == "+findByTokenValue.get().getTokenValue());
    System.out.println("blackList안에 들어간 time == "+findByTokenValue.get().getExpirationTime());
    assertThat(findByTokenValue.get().getTokenValue()).isEqualTo(refreshToken);

  }

}