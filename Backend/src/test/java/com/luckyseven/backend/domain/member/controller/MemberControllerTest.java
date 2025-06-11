package com.luckyseven.backend.domain.member.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.luckyseven.backend.domain.member.dto.LoginMemberRequest;
import com.luckyseven.backend.domain.member.dto.RegisterMemberRequest;
import com.luckyseven.backend.domain.member.entity.Member;
import com.luckyseven.backend.domain.member.repository.MemberRepository;
import com.luckyseven.backend.sharedkernel.jwt.repository.BlackListTokenRepository;
import jakarta.servlet.http.Cookie;
import java.util.Arrays;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MemberControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private MemberRepository memberRepository;

  @Autowired
  private PasswordEncoder passwordEncoder;

  @Autowired
  private BlackListTokenRepository blackListTokenRepository;

  @AfterEach
  void cleanUp(){
    memberRepository.deleteAll();
  }

  @Test
  @DisplayName("POST /api/user/register — 성공 시 201, DB에 저장되고 응답 body = success")
  void registerMember_success() throws Exception {

    RegisterMemberRequest req = RegisterMemberRequest.builder()
        .email("test@test.com")
        .password("1234A1234")
        .checkPassword("1234A1234")
        .nickname("testUser")
        .build();


    mockMvc.perform(post("/api/users/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(req)))
        .andExpect(status().isCreated())
        .andExpect(content().string("test@test.com"));


    boolean exists = memberRepository.findByEmail("test@test.com").isPresent();
    assertThat(exists).isTrue();
    Member saved = memberRepository.findByEmail("test@test.com").get();
    assertThat(saved.getNickname()).isEqualTo("testUser");
  }

  @Test
  @DisplayName("POST /api/user/register — 잘못된 이메일 형식,400")
  void registerMember_email_validate_error() throws Exception {
    RegisterMemberRequest req = RegisterMemberRequest.builder()
        .email("test")
        .password("1234")
        .checkPassword("1234")
        .nickname("testUser")
        .build();


    MvcResult mvc = mockMvc.perform(post("/api/users/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(req)))
        .andExpect(status().isBadRequest())
        .andReturn();

    String json = mvc.getResponse().getContentAsString();
    System.out.println(">>>> 응답 JSON >>>> " + json);
  }

  @Test
  @DisplayName("POST /api/user/register — 잘못된 이메일 형식,400")
  void registerMember_password_validate_error() throws Exception {

    RegisterMemberRequest req = RegisterMemberRequest.builder()
        .email("test@test.com")
        .password("1234")
        .checkPassword("1234")
        .nickname("testUser")
        .build();


    mockMvc.perform(post("/api/users/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(req)))
        .andExpect(status().isBadRequest());
  }





  @Test
  @DisplayName("POST /api/users/checkNickname - 중복 있으면 409 conflict")
  void checkNickname_duplicate_returns409() throws Exception {
    String duplicateNickname = "duplicateNickname";
    memberRepository.save(Member.builder()
        .email("test3@test.com")
        .password("1234")
        .nickname(duplicateNickname)
        .build());

    MvcResult mvc = mockMvc.perform(post("/api/users/checkNickname")
            .param("nickname", duplicateNickname))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.code").value("MEMBER_NICKNAME_DUPLICATE"))
        .andReturn();

    String json = mvc.getResponse().getContentAsString();
    System.out.println(">>>> 응답 JSON >>>> " + json);
  }



  @Test
  @DisplayName("POST /api/user/checkEmail?email=… — 중복 있으면 409 Conflict")
  void checkEmail_duplicate_returns409() throws Exception {

    String duplicateEmail = "duplicate@test.com";
    memberRepository.save(Member.builder()
        .email(duplicateEmail)
        .password("1234")
        .nickname("testNick")
        .build());


    mockMvc.perform(post("/api/users/checkEmail")
            .param("email", duplicateEmail))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.code").value("MEMBER_EMAIL_DUPLICATE"));
  }

  @Test
  @DisplayName("POST /api/users/checkPassword - 일치하지 않으면 403")
  void checkPassword_duplicate_returns403() throws Exception {
    mockMvc.perform(post("/api/users/checkPassword")
            .param("password", "1234")
            .param("checkPassword", "12345"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.code").value("MEMBER_PASSWORD_MISMATCH"));
  }

  @Test
  @DisplayName("POST /api/users/login - 올바른 인증 정보면 200 OK")
  void login_success() throws Exception {
    String email = "test@test.com";
    String password = "1234";
    memberRepository.save(Member.builder()
        .email(email)
        .password(passwordEncoder.encode(password))
        .nickname(password)
        .build());

    LoginMemberRequest req = new LoginMemberRequest(email,password);

    MvcResult mvc = mockMvc.perform(post("/api/users/login")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(req)))
        .andExpect(status().isOk())
            .andReturn();

    System.out.println("======= 헤더 응답입니다. ========");
    mvc.getResponse().getHeaderNames()
        .forEach(h -> System.out.println(h +  ": " + mvc.getResponse().getHeaderValue(h)));

    System.out.println("========== cookie 응답입니다. =========");
    Arrays.stream(mvc.getResponse().getCookies()).forEach(c ->
        System.out.printf("%s = %s; path=%s; httpOnly=%s; sameSite=%s%n",
            c.getName(), c.getValue(), c.getPath(), c.isHttpOnly(),
            c.getAttribute("SameSite"))
    );

  }
  @Test
  @DisplayName("POST /api/users/login - 비밀번호가 불일치면 401 Unauthorized , 오류 코드..?는 확인해봐야할듯")
  void login_wrongPassword_return401() throws Exception {
    String email = "login@test.com";
    memberRepository.save(Member.builder()
            .email(email)
            .password("correctPwd")
            .nickname("user2")
            .build());

    LoginMemberRequest req = new LoginMemberRequest(email,"wrongPwd");

    mockMvc.perform(post("/api/users/login")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(req)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("AUTHENTICATION_FAILED"));
  }

  @Test
  @DisplayName("POST /api/users/login - 존재하지 않는 이메일로 로그인시 404 NOTFOUNND , 코드 MEMBER_EMAIL_NOTFOUND")
  void login_nonexistentEmail_return404() throws Exception {
    LoginMemberRequest req = new LoginMemberRequest("test@test.com","anyPwd");

    mockMvc.perform(post("/api/users/login")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(req)))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value("MEMBER_EMAIL_NOTFOUND"));

  }

  @Test
  @DisplayName("POST /api/users/logout")
  void logout_success() throws Exception {
    String email = "test@test.com";
    String password = "1234";
    String nickname = "test";
    Member member = Member.builder()
        .email(email)
        .password(passwordEncoder.encode(password))
        .nickname(nickname)
        .build();
    memberRepository.save(member);

    LoginMemberRequest req = new LoginMemberRequest(email,password);
    MvcResult loginResult = mockMvc.perform(post("/api/users/login")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(req)))
        .andExpect(status().isOk())
            .andReturn();

    String authHeader = loginResult.getResponse().getHeader(HttpHeaders.AUTHORIZATION);
    String accessToken = authHeader.substring("Bearer ".length());
    Cookie refreshCookie = Arrays.stream(loginResult.getResponse().getCookies())
        .filter(cookie -> "refreshToken".equals(cookie.getName()))
        .findFirst()
        .orElseThrow(() -> new IllegalStateException("refresh 쿠키 없음"));
    String refreshToken = refreshCookie.getValue();

    MvcResult logoutResult = mockMvc.perform(post("/api/users/logout")
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
        .cookie(refreshCookie))
        .andExpect(status().isNoContent())
            .andReturn();

    Optional<Cookie> logoutRefreshCookie = Arrays.stream(logoutResult.getResponse().getCookies())
            .filter(cookie -> "refreshToken".equals(cookie.getName()))
                .findFirst();

    System.out.println("로그인이 된 RefreshToken == "  + refreshToken);
    System.out.println("로그아웃이 된 RefreshToken == " + logoutRefreshCookie.get().getValue());
    System.out.println("로그인 헤더 " + loginResult.getResponse().getHeader(HttpHeaders.AUTHORIZATION).substring(7));
    System.out.println("로그아윳 헤더 " + logoutResult.getResponse().getHeader(HttpHeaders.AUTHORIZATION).substring(0));
    assertThat(blackListTokenRepository.existsByTokenValue(refreshToken)).isTrue();
  }
}