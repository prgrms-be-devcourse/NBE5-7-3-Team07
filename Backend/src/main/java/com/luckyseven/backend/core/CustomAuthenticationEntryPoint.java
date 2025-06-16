package com.luckyseven.backend.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.luckyseven.backend.sharedkernel.exception.CustomLogicException;
import com.luckyseven.backend.sharedkernel.exception.ErrorResponse;
import com.luckyseven.backend.sharedkernel.exception.ExceptionCode;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import lombok.extern.slf4j.Slf4j;


import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

  private final ObjectMapper objectMapper;

  public CustomAuthenticationEntryPoint(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }


  //TODO : JWT.parse에 있는 것과 중복로직이 아닌가?
  @Override
  public void commence(HttpServletRequest request, HttpServletResponse response,
      AuthenticationException authException) throws IOException, ServletException {

    log.info("authException.getMessage() == {}", authException.getMessage());
    CustomLogicException ex = new CustomLogicException(
        ExceptionCode.AUTHENTICATION_FAILED,
        authException.getMessage()
    );

    ErrorResponse error = ErrorResponse.Companion.from(ex);
    response.setContentType("application/json;charset=UTF-8");
    response.setStatus(ex.getExceptionCode().getHttpStatus().value());
    try (PrintWriter w = response.getWriter()) {
      w.write(objectMapper.writeValueAsString(error));
    }
  }
}
