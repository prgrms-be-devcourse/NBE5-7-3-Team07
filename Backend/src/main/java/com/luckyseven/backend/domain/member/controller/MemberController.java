package com.luckyseven.backend.domain.member.controller;

import com.luckyseven.backend.domain.member.dto.LoginMemberRequest;
import com.luckyseven.backend.domain.member.dto.RegisterMemberRequest;
import com.luckyseven.backend.domain.member.service.MemberService;
import com.luckyseven.backend.sharedkernel.exception.CustomLogicException;
import com.luckyseven.backend.sharedkernel.exception.ExceptionCode;
import com.luckyseven.backend.sharedkernel.jwt.utill.JwtTokenizer;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping({"/api/users"})
public class MemberController implements MemberApi {
  private final MemberService service;
  private final PasswordEncoder passwordEncoder;



  @Override
  @PostMapping("/register")
  public ResponseEntity<String> registerMember(@RequestBody RegisterMemberRequest req) {
    /* 이걸 @Validate를 해서 Controller에서 처리를 할지 @Validate를 뺴고 Service에서 처리를 할지 고민
    *  if (bindingResult.hasErrors()) {
      FieldError fe = bindingResult.getFieldErrors().get(0);
      String field = fe.getField();
      String defaultMsg = fe.getDefaultMessage();

      // 2) 필드별로 커스텀 에러코드 던지기
      switch (field) {
        case "email":
          throw new CustomLogicException(
            ExceptionCode.INVALID_EMAIL_FORMAT, defaultMsg);
        case "password":
          throw new CustomLogicException(
            ExceptionCode.INVALID_PASSWORD_FORMAT, defaultMsg);
        case "checkPassword":
          throw new CustomLogicException(
            ExceptionCode.INVALID_CHECKPASSWORD_FORMAT, defaultMsg);
        case "nickname":
          throw new CustomLogicException(
            ExceptionCode.BAD_REQUEST, defaultMsg);
        default:
          throw new CustomLogicException(
            ExceptionCode.BAD_REQUEST, defaultMsg);
      }
    }*/
    log.info("==========");
    log.info("req.email() == {}", req.email());
    log.info("req.password() == {}", req.password());
    log.info("req.checkPassword() == {}", req.checkPassword());
    log.info("req.nickname() == {}", req.nickname());
    String email = service.registerMember(req,passwordEncoder);
    return ResponseEntity.status(HttpStatus.CREATED).body(email);
  }

  //TODO <추후 고려사항> : 사용자가 중간에 이메일을 바꿀경우 boolean형으로?
  @Override
  @PostMapping("/checkEmail")
  public ResponseEntity<Void> checkEmail(@RequestParam String email) {
    service.checkDuplicateEmail(email);
    return ResponseEntity.noContent().build();
  }

  //TODO <추후 고려사항> : 사용자가 중간에 닉네임를 바꿀경우 boolean형으로?
  @Override
  @PostMapping("/checkNickname")
  public ResponseEntity<Void> checkNickName(@RequestParam String nickname) {
    service.checkDuplicateNickName(nickname);
    return ResponseEntity.noContent().build();
  }

  //TODO <추후 고려사항> : 사용자가 중간에 비밀번호를 바꿀경우 boolean형으로?
  @Override
  @PostMapping("/checkPassword")
  public ResponseEntity<Void> checkPassword(@RequestParam String password,@RequestParam String checkPassword) {
    service.checkEqualsPassword(password,checkPassword);
    return ResponseEntity.noContent().build();
  }


  @PostMapping("/logout")
  public ResponseEntity<Void> logout(@CookieValue(name = "refreshToken") String refreshToken, HttpServletResponse resp) {
    service.logout(refreshToken,resp);
    resp.setHeader("Authorization", "");
    resp.setHeader("Access-Control-Expose-Headers", "Authorization");
    return ResponseEntity.noContent().build();
  }


  @PostMapping("/login")
    public ResponseEntity<Void> login(@RequestBody LoginMemberRequest req,
      HttpServletResponse resp){
    String token = service.Login(req,resp);
    return ResponseEntity.ok().header("Authorization","Bearer "+token).build();
  }
}
