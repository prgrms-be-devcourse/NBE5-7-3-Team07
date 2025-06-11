package com.luckyseven.backend.sharedkernel.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
@RestControllerAdvice
@Slf4j
public class CustomExceptionHandler {

  @ExceptionHandler(CustomLogicException.class)
  protected ResponseEntity<ErrorResponse> handleCustomException(
      HttpServletRequest request,         // 요청 정보도 같이 받아오고
      final CustomLogicException e
  ) {
    // 1) ErrorResponse 생성
    ErrorResponse error = ErrorResponse.of(e);

    // 2) 콘솔(혹은 파일)에 딱 한 줄로 요약된 로그를 남깁니다
    log.error(
        "Exception @ {} {} → code: {}, message: {}, detail: {}",
        request.getMethod(),
        request.getRequestURI(),
        error.getCode(),
        error.getMessage(),
        error.getDetail()
    );

    // 3) 클라이언트에 응답
    return ResponseEntity
        .status(e.getExceptionCode().getHttpStatus())
        .body(error);
  }
}