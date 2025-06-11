package com.luckyseven.backend.sharedkernel.exception;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ErrorResponse {

  private final String code;
  private final String message;
  private final int status;
  private final String detail;

  public static ErrorResponse of(CustomLogicException e) {
    return new ErrorResponse(
        e.getExceptionCode().name(),
        e.getExceptionCode().getMessage(),
        e.getExceptionCode().getHttpStatus().value(),
        e.getDetail()
    );
  }
}
