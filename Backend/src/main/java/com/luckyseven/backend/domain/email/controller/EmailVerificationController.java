package com.luckyseven.backend.domain.email.controller;

import com.luckyseven.backend.domain.email.dto.EmailRequest;
import com.luckyseven.backend.domain.email.service.EmailService;
import com.luckyseven.backend.domain.email.service.EmailVerificationService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.thymeleaf.context.Context;


@RestController
@RequestMapping("/api/email")
@RequiredArgsConstructor
@Slf4j
public class EmailVerificationController {
  private final EmailVerificationService verificationService;
  private final EmailService emailService;

  @PostMapping("/request-email")
  public ResponseEntity<String> requestEmail(@Valid @RequestBody EmailRequest req){
    String email = req.email();
    log.debug("회원가입 요청 받음 {}" , req.email());
    String token = verificationService.generateAndSaveToken(email);
    String link = "http://localhost:3000/email-verify?token="+token;
    Context ctx = new Context();
    ctx.setVariable("email", email);
    ctx.setVariable("link", link);

    emailService.sendTemplateEmail(
        email,
        "회원 가입 이메일 인증 요청",
        "email-verification",
        ctx
    );

    return ResponseEntity.created(URI.create("/api/auth/request-email"))
        .body("인증 메일을 발송했습니다.");
  }

  @GetMapping("/verify")
  public ResponseEntity<Map<String, Object>> verifyEmail(@RequestParam String token, HttpServletResponse response)
      throws IOException {
    log.info("이메일 토큰 검증 요청: {}", token);

    String email = verificationService.validateToken(token);
    Map<String, Object> result = new HashMap<>();

    if (email != null) {
      verificationService.deleteTokenByEmail(email);
      log.info("이메일 인증 성공: {}", email);
      result.put("success", true);
      result.put("email", email);
      result.put("message", "이메일 인증이 완료되었습니다.");
      return ResponseEntity.ok(result);
    } else {
      log.info("이메일 인증 실패");
      result.put("success", false);
      result.put("message", "유효하지 않은 토큰입니다.");
      return ResponseEntity.badRequest().body(result);
    }
  }




}
