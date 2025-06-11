package com.luckyseven.backend.core;

import com.luckyseven.backend.sharedkernel.exception.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
@Component
public class LoggingInterceptor implements HandlerInterceptor {
  @Override
  public boolean preHandle(HttpServletRequest request,
      HttpServletResponse response,
      Object handler) {
    System.out.println("=========  Incoming Request Data =========");
    System.out.println("Request URL    : " + request.getRequestURL());
    System.out.println("Request Method : " + request.getMethod());
    System.out.println("Query Params   : " + request.getQueryString());
    return true;
  }

  @Override
  public void afterCompletion(HttpServletRequest request,
      HttpServletResponse response,
      Object handler,
      Exception ex) {
    System.out.println("=========  Outgoing Response Data =========");
    System.out.println("Response Status : " + response.getStatus());
    System.out.println("Request URL      : " + request.getRequestURL());
    System.out.println("Request Method   : " + request.getMethod());
    System.out.println("Query Params     : " + request.getQueryString());

    //  request 에 담긴 ErrorResponse 꺼내기
    Object obj = request.getAttribute("errorResponse");
    if (obj instanceof ErrorResponse err) {
      System.out.println("Error Code    : " + err.getCode());
      System.out.println("Error Message : " + err.getMessage());
      System.out.println("Error Detail  : " + err.getDetail());
    }
  }
}