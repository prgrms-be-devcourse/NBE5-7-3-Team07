package com.luckyseven.backend.domain.member.controller

import com.luckyseven.backend.domain.member.dto.LoginMemberRequest
import com.luckyseven.backend.domain.member.dto.RegisterMemberRequest
import com.luckyseven.backend.domain.member.service.MemberService
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/users")
class MemberController(
    private val service: MemberService,
    private val passwordEncoder: PasswordEncoder
) : MemberApi {
    
    private val logger = LoggerFactory.getLogger(MemberController::class.java)
    
    @PostMapping("/register")
    override fun registerMember(@RequestBody req: RegisterMemberRequest): ResponseEntity<String> {
        logger.info("==========")
        logger.info("req.email() == {}", req.email)
        logger.info("req.password() == {}", req.password)
        logger.info("req.checkPassword() == {}", req.checkPassword)
        logger.info("req.nickname() == {}", req.nickname)
        
        val email = service.registerMember(req, passwordEncoder)
        return ResponseEntity.status(HttpStatus.CREATED).body(email)
    }
    
    //TODO <추후 고려사항> : 사용자가 중간에 이메일을 바꿀경우 boolean형으로?
    @PostMapping("/checkEmail")
    override fun checkEmail(@RequestParam email: String): ResponseEntity<Void> {
        service.checkDuplicateEmail(email)
        return ResponseEntity.noContent().build()
    }
    
    //TODO <추후 고려사항> : 사용자가 중간에 닉네임을 바꿀경우 boolean형으로?
    @PostMapping("/checkNickname")
    override fun checkNickName(@RequestParam nickname: String): ResponseEntity<Void> {
        service.checkDuplicateNickName(nickname)
        return ResponseEntity.noContent().build()
    }
    
    //TODO <추후 고려사항> : 사용자가 중간에 비밀번호를 바꿀경우 boolean형으로?
    @PostMapping("/checkPassword")
    override fun checkPassword(
        @RequestParam password: String,
        @RequestParam checkPassword: String
    ): ResponseEntity<Void> {
        service.checkEqualsPassword(password, checkPassword)
        return ResponseEntity.noContent().build()
    }
    
    @PostMapping("/logout")
    override fun logout(
        @CookieValue(name = "refreshToken") refreshToken: String,
        resp: HttpServletResponse
    ): ResponseEntity<Void> {
        service.logout(refreshToken, resp)
        resp.setHeader("Authorization", "")
        resp.setHeader("Access-Control-Expose-Headers", "Authorization")
        return ResponseEntity.noContent().build()
    }
    
    @PostMapping("/login")
    fun login(
        @RequestBody req: LoginMemberRequest,
        resp: HttpServletResponse
    ): ResponseEntity<Void> {
        val token = service.login(req, resp)
        return ResponseEntity.ok().header("Authorization", "Bearer $token").build()
    }
} 