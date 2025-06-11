package com.luckyseven.backend.sharedkernel.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ExceptionCode {
  /*
   * 400 BAD_REQUEST: 잘못된 요청
   */
  BAD_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),
  BAD_FILE_EXTENSION(HttpStatus.BAD_REQUEST, "파일 형식이 올바르지 않습니다."),
  TEAM_PASSWORD_MISMATCH(HttpStatus.BAD_REQUEST, "팀 비밀번호가 일치하지 않습니다."),
  ALREADY_TEAM_MEMBER(HttpStatus.BAD_REQUEST, "이미 팀에 가입된 회원입니다."),
  NOT_TEAM_MEMBER(HttpStatus.BAD_REQUEST, "해당 팀에 속한 멤버가 아닙니다."),
  INSUFFICIENT_BALANCE(HttpStatus.BAD_REQUEST, "예산이 부족합니다."),

  /*
   * 403 FORBIDDEN: 승인을 거부함
   */
  ROLE_FORBIDDEN(HttpStatus.FORBIDDEN, "권한이 없는 역할입니다."),

  /*
   * 404 NOT_FOUND: 리소스를 찾을 수 없음
   */
  TEAM_NOT_FOUND(HttpStatus.NOT_FOUND, "팀을 찾을 수 없습니다."),
  TEAM_MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "팀 멤버를 찾을 수 없습니다."),
  BUDGET_NOT_FOUND(HttpStatus.NOT_FOUND, "예산 정보를 찾을 수 없습니다."),
  USER_NOT_FOUND(HttpStatus.NOT_FOUND, "회원을 찾을 수 없습니다."),
  EXPENSE_PAYER_NOT_FOUND(HttpStatus.NOT_FOUND, "결제자를 찾을 수 없습니다."),
  EXPENSE_SETTLER_NOT_FOUND(HttpStatus.NOT_FOUND, "정산 대상자를 찾을 수 없습니다."),
  EXPENSE_NOT_FOUND(HttpStatus.NOT_FOUND, "지출 내역을 조회할 수 없습니다."),
  SETTLEMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "정산내역을 찾을 수 없습니다."),

  /*
   * 405 METHOD_NOT_ALLOWED: 허용되지 않은 Request Method 호출
   */
  METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "허용되지 않은 메서드입니다."),

  /*
   * 409 CONFLICT
   */
  USERNAME_CONFLICT(HttpStatus.CONFLICT, "존재하는 회원 이름입니다."),
  BUDGET_CONFLICT(HttpStatus.CONFLICT, "팀에 이미 존재하는 예산이 있습니다."),

  /*
   * 500 INTERNAL_SERVER_ERROR: 내부 서버 오류
   */
  INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "내부 서버 오류입니다."),
  API_CALL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "API 호출에 실패했습니다."),

  //MEMBER 에러입니다.
  MEMBER_EMAIL_DUPLICATE(HttpStatus.CONFLICT,"이메일 중복입니다.:%s"),
  MEMBER_NICKNAME_DUPLICATE(HttpStatus.CONFLICT,"닉네임 중복입니다.:%s"),
  MEMBER_PASSWORD_MISMATCH(HttpStatus.UNAUTHORIZED,"비밀번호 확인 결과가 맞지 않습니다."),
  MEMBER_EMAIL_NOTFOUND(HttpStatus.NOT_FOUND,"이메일을 찾을 수 없습니다.:%s"),
  MEMBER_ID_NOTFOUND(HttpStatus.NOT_FOUND,"회원ID를 찾을 수 없습니다."),

  //로그인 관련 에러입니다.
  AUTHENTICATION_FAILED(HttpStatus.BAD_REQUEST,"로그인을 할 수 없습니다."),

  //JWT 관련 에러입니다.
  JWT_TOKEN_NOT_FOUND(HttpStatus.NOT_FOUND,"토큰을 찾을 수 없습니다."),
  JWT_EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED,"토큰이 만료되었습니다."),
  JWT_INVALID_TOKEN(HttpStatus.BAD_REQUEST,"유효하지 않는 토큰입니다."),
  JWT_BLACKLISTED_TOKEN(HttpStatus.BAD_REQUEST,"블랙리스트에 등록된 토큰입니다."),

  //Validator
  INVALID_EMAIL_FORMAT(HttpStatus.BAD_REQUEST,"올바르지 않은 이메일 형식입니다."),
  INVALID_PASSWORD_FORMAT(HttpStatus.BAD_REQUEST,"올바르지 않은 비밀번호 형식입니다."),
  INVALID_CHECKPASSWORD_FORMAT(HttpStatus.BAD_REQUEST,"올바르지 않은 비밀번호 형식입니다.");


  private final HttpStatus httpStatus;
  private final String message;

  ExceptionCode(HttpStatus httpStatus, String message) {
    this.httpStatus = httpStatus;
    this.message = message;
  }
}
