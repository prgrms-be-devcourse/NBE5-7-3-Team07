package com.luckyseven.backend.domain.email.controller

import com.luckyseven.backend.domain.email.dto.EmailRequest
import com.luckyseven.backend.domain.email.service.EmailService
import com.luckyseven.backend.domain.email.service.EmailVerificationService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.thymeleaf.context.Context
import java.io.IOException
import java.net.URI

@RestController
@RequestMapping("/api/email")
@Tag(name = "Email", description = "이메일 인증 관련 API")
class EmailController(
    private val verificationService: EmailVerificationService,
    private val emailService: EmailService
) {
    
    private val logger = LoggerFactory.getLogger(EmailController::class.java)
    
    @PostMapping("/request-email")
    @Operation(summary = "이메일 인증 요청", description = "회원가입을 위한 이메일 인증 요청을 발송합니다.")
    @ApiResponse(responseCode = "201", description = "이메일 발송 성공")
    fun requestEmail(@Valid @RequestBody req: EmailRequest): ResponseEntity<String> {
        val email = req.email
        logger.debug("회원가입 요청 받음 {}", req.email)
        
        val token = verificationService.generateAndSaveToken(email)
        val link = "http://travelexpensemanager.kro.kr/email-verify?token=$token"
        
        val ctx = Context()
        ctx.setVariable("email", email)
        ctx.setVariable("link", link)
        
        emailService.sendTemplateEmail(
            email,
            "회원 가입 이메일 인증 요청",
            "email-verification",
            ctx
        )
        
        return ResponseEntity.created(URI.create("/api/auth/request-email"))
            .body("인증 메일을 발송했습니다.")
    }
    
    @GetMapping("/verify")
    @Operation(summary = "이메일 인증 확인", description = "토큰을 통해 이메일 인증을 확인합니다.")
    @ApiResponse(responseCode = "200", description = "이메일 인증 성공")
    fun verifyEmail(
        @Parameter(description = "인증 토큰") @RequestParam token: String,
        response: HttpServletResponse
    ): ResponseEntity<Map<String, Any>> {
        logger.info("이메일 토큰 검증 요청: {}", token)
        
        val email = verificationService.validateToken(token)
        val result = mutableMapOf<String, Any>()
        
        return if (email != null) {
            verificationService.deleteTokenByEmail(email)
            logger.info("이메일 인증 성공: {}", email)
            result["success"] = true
            result["email"] = email
            result["message"] = "이메일 인증이 완료되었습니다."
            ResponseEntity.ok(result)
        } else {
            logger.info("이메일 인증 실패")
            result["success"] = false
            result["message"] = "유효하지 않은 토큰입니다."
            ResponseEntity.badRequest().body(result)
        }
    }
} 