package com.luckyseven.backend.sharedkernel.exception;


import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class CustomLogicException extends RuntimeException {
  private final ExceptionCode exceptionCode;
  private final String detail;
  public CustomLogicException(ExceptionCode exceptionCode, String detail) {
    super(exceptionCode.getMessage());
    this.exceptionCode = exceptionCode;
    this.detail = detail;
  }

  public CustomLogicException(ExceptionCode exceptionCode) {
    super(exceptionCode.getMessage());
    this.exceptionCode = exceptionCode;
    this.detail = "";
  }

  public CustomLogicException(ExceptionCode exceptionCode, Object... args) {
    super(String.format(exceptionCode.getMessage(), args));
    this.exceptionCode = exceptionCode;
    this.detail = "";
  }

}
