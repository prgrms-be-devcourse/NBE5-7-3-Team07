package com.luckyseven.backend.domain.email.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

  private final JavaMailSender    mailSender;
  private final TemplateEngine    templateEngine;

  @Value("${spring.mail.username}")
  private String fromEmail;

  /**
   * 이메일 인증용 HTML 템플릿을 처리해서 보내줍니다.
   *
   * @param toEmail  수신자 이메일
   * @param subject  메일 제목
   * @param templateName  templates 디렉토리 아래의 Thymeleaf 템플릿 이름 (확장자 제외)
   * @param emailVars     템플릿에 바인딩할 변수들(예: name, link 등)
   */
  public void sendTemplateEmail(
      String toEmail,
      String subject,
      String templateName,
      Context emailVars
  ) {
    try {

      String htmlBody = templateEngine.process(templateName, emailVars);

      MimeMessage message = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
      helper.setFrom(fromEmail);
      helper.setTo(toEmail);
      helper.setSubject(subject);
      helper.setText(htmlBody, true);


      mailSender.send(message);

      log.info("Sent template email to {}", toEmail);
    } catch (MessagingException e) {
      log.error("이메일 발송 실패: {}", e.getMessage(), e);
      throw new RuntimeException("이메일 발송 실패", e);
    }
  }
}