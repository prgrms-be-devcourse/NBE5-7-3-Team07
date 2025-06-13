package com.luckyseven.backend.sharedkernel.exception

import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class CustomExceptionHandler {
    private val log = LoggerFactory.getLogger(CustomExceptionHandler::class.java)

    @ExceptionHandler(CustomLogicException::class)
    protected fun handleCustomException(
        request: HttpServletRequest,         // 요청 정보도 같이 받아오고
        e: CustomLogicException
    ): ResponseEntity<ErrorResponse> {
        // 1) ErrorResponse 생성
        val error = ErrorResponse.from(e)

        // 2) 콘솔(혹은 파일)에 딱 한 줄로 요약된 로그를 남깁니다
        log.error(
            "Exception @ {} {} → code: {}, message: {}, detail: {}",
            request.method,
            request.requestURI,
            error.code,
            error.message,
            error.detail
        )

        // 3) 클라이언트에 응답
        return ResponseEntity
            .status(e.exceptionCode.httpStatus)
            .body(error)
    }
}
