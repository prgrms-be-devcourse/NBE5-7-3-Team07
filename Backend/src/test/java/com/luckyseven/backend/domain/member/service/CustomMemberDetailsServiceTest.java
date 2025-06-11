package com.luckyseven.backend.domain.member.service;

import static org.junit.jupiter.api.Assertions.*;

import com.luckyseven.backend.domain.member.dto.RegisterMemberRequest;
import com.luckyseven.backend.domain.member.repository.MemberRepository;
import com.luckyseven.backend.sharedkernel.exception.CustomLogicException;
import com.luckyseven.backend.sharedkernel.exception.ExceptionCode;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootTest
@Transactional
@Slf4j
class CustomMemberDetailsServiceTest {
  @Autowired
  private MemberService service;
  @Autowired
  private MemberRepository memberRepository;
  @Autowired
  private PasswordEncoder passwordEncoder;

  @Test
  @DisplayName("회원가입 테스트")
  void register_member_Test() {
    RegisterMemberRequest request = RegisterMemberRequest.builder()
        .email("test@test.com")
        .password("1234AA56")
        .checkPassword("1234AA56")
        .nickname("test")
        .build();


    service.registerMember(request,passwordEncoder);

    System.out.println(memberRepository.findByEmail("test@test.com").get().getId());
    System.out.println(memberRepository.findByNickname("test").get().getEmail());
    System.out.println(memberRepository.findByEmail("test@test.com").get().getNickname());
    System.out.println(memberRepository.findByEmail("test@test.com").get().getPassword());

  }
  @Test
  @DisplayName("회원가입 이메일 중복 테스트")
  void register_member_email_duplicate() {
    String duplicateEmail = "test@test.com";
    RegisterMemberRequest request= RegisterMemberRequest.builder()
        .email(duplicateEmail)
        .password("123456A")
        .checkPassword("123456A")
        .nickname("test")
        .build();
    service.registerMember(request,passwordEncoder);

    RegisterMemberRequest newRequest = RegisterMemberRequest.builder()
        .email(duplicateEmail)
        .password("123456A")
        .checkPassword("123456A")
        .nickname("test1")
        .build();
    CustomLogicException ex = assertThrows(
        CustomLogicException.class,
        () -> service.registerMember(
            newRequest,
            passwordEncoder
        ),
        "이메일 중복 시 예외가 던져져야 합니다."
    );
    System.out.println(ex.getMessage());
    assertEquals(
        ExceptionCode.MEMBER_EMAIL_DUPLICATE,
        ex.getExceptionCode(),
        "ExceptionCode == MEMBER_EMAIL_DUPLICATE"
    );
    assertEquals(
        String.format("이메일 중복입니다.:%s", duplicateEmail),
        ex.getMessage()
    );
  }

  @Test
  @DisplayName("회원가입 - 닉네임 중복")
  void register_member_nickname_duplicate() {
    String duplicateNickname = "DuplicateNickname";
    RegisterMemberRequest request = RegisterMemberRequest.builder()
        .email("test@test.com")
        .password("123456A")
        .checkPassword("123456A")
        .nickname(duplicateNickname)
        .build();
    service.registerMember(request,passwordEncoder);

    RegisterMemberRequest newRequest = RegisterMemberRequest.builder()
        .email("test1@test.com")
        .password("123456A")
        .checkPassword("123456A")
        .nickname(duplicateNickname)
        .build();

    CustomLogicException ex = assertThrows(
        CustomLogicException.class,
        () -> service.registerMember(
            newRequest,
            passwordEncoder
        ),
        "닉네임 중복시 예외가 던져져야합니다."
    );
    System.out.println(ex.getMessage());
    assertEquals(
        ExceptionCode.MEMBER_NICKNAME_DUPLICATE,
        ex.getExceptionCode(),
        "ExceptionCode == MEMBER_NICKNAME_DUPLICATE"
    );
    assertEquals(
        String.format("닉네임 중복입니다.:%s",duplicateNickname),
        ex.getMessage()
    );
  }
  @Test
  @DisplayName("비밀번호 확인 오류")
  void register_member_password_Define() {
    RegisterMemberRequest register = RegisterMemberRequest.builder()
        .email("test@test.com")
        .password("123456A")
        .checkPassword("1234567ASC")
        .nickname("test")
        .build();
    CustomLogicException ex = assertThrows(
        CustomLogicException.class,
        () -> service.registerMember(
            register,
            passwordEncoder
        )
        ,"비밀번호 확인 결과가 맞지않다는 오류가 던져져야합니다."
    );
    System.out.println(ex.getMessage());
    assertEquals(
        ExceptionCode.MEMBER_PASSWORD_MISMATCH,
        ex.getExceptionCode(),
        "ExceptionCode == MEMBER_PASSWORD_MISMATCH"
    );
    assertEquals(
        String.format("비밀번호 확인 결과가 맞지 않습니다."),
        ex.getMessage()
    );
  }
}