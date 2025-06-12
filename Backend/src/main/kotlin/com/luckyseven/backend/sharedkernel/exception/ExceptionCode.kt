package com.luckyseven.backend.sharedkernel.exception

import org.springframework.http.HttpStatus

enum class ExceptionCode(
    val httpStatus: HttpStatus,
    val message: String
) {
    // Member 관련
    MEMBER_ID_NOTFOUND(HttpStatus.NOT_FOUND, "Member not found"),
    MEMBER_EMAIL_NOTFOUND(HttpStatus.NOT_FOUND, "Member email not found"),
    MEMBER_EMAIL_DUPLICATE(HttpStatus.CONFLICT, "Email already exists"),
    MEMBER_NICKNAME_DUPLICATE(HttpStatus.CONFLICT, "Nickname already exists"),
    MEMBER_PASSWORD_MISMATCH(HttpStatus.BAD_REQUEST, "Password mismatch"),
    
    // Validation 관련
    INVALID_EMAIL_FORMAT(HttpStatus.BAD_REQUEST, "Invalid email format"),
    INVALID_PASSWORD_FORMAT(HttpStatus.BAD_REQUEST, "Invalid password format"),
    INVALID_CHECKPASSWORD_FORMAT(HttpStatus.BAD_REQUEST, "Invalid check password format"),
    
    // JWT 관련
    JWT_EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "JWT token expired"),
    JWT_INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "Invalid JWT token"),
    JWT_BLACKLISTED_TOKEN(HttpStatus.UNAUTHORIZED, "Blacklisted JWT token"),
    JWT_TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED, "JWT token not found"),
    
    // 인증 관련
    AUTHENTICATION_FAILED(HttpStatus.UNAUTHORIZED, "Authentication failed"),
    
    // 기본 오류
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "Bad request"),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error")
} 