package com.luckyseven.backend.domain.email.service

import jakarta.mail.internet.MimeMessage
import org.slf4j.LoggerFactory
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.thymeleaf.TemplateEngine
import org.thymeleaf.context.Context

@Service
class EmailService(
    private val javaMailSender: JavaMailSender,
    private val templateEngine: TemplateEngine
) {
    
    private val logger = LoggerFactory.getLogger(EmailService::class.java)
    
    @Async
    fun sendTemplateEmail(
        email: String,
        subject: String,
        templateName: String,
        context: Context
    ) {
        try {
            val mimeMessage: MimeMessage = javaMailSender.createMimeMessage()
            val helper = MimeMessageHelper(mimeMessage, true, "UTF-8")
            
            val htmlContent = templateEngine.process(templateName, context)
            
            helper.setTo(email)
            helper.setSubject(subject)
            helper.setText(htmlContent, true)
            helper.setFrom("noreply@luckyseven.com")
            
            javaMailSender.send(mimeMessage)
            logger.info("이메일 발송 완료: {} -> {}", subject, email)
            
        } catch (e: Exception) {
            logger.error("이메일 발송 실패: {} -> {}", subject, email, e)
            throw e
        }
    }
} 