package com.luckyseven.backend.sharedkernel.exception

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class CustomExceptionHandler {
    @ExceptionHandler(CustomLogicException::class)
    fun handleCustomLogicException(e: CustomLogicException): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse.from(e)
        val httpStatus = e.exceptionCode.httpStatus

        return ResponseEntity(errorResponse, httpStatus)
    }
}