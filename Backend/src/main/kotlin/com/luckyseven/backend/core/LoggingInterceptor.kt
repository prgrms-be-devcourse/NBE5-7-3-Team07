package com.luckyseven.backend.core

import com.luckyseven.backend.sharedkernel.exception.ErrorResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor

@Component
class LoggingInterceptor : HandlerInterceptor {
    
    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any
    ): Boolean {
        println("=========  Incoming Request Data =========")
        println("Request URL    : ${request.requestURL}")
        println("Request Method : ${request.method}")
        println("Query Params   : ${request.queryString}")
        return true
    }
    
    override fun afterCompletion(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
        ex: Exception?
    ) {
        println("=========  Outgoing Response Data =========")
        println("Response Status : ${response.status}")
        println("Request URL      : ${request.requestURL}")
        println("Request Method   : ${request.method}")
        println("Query Params     : ${request.queryString}")
        
        // request 에 담긴 ErrorResponse 꺼내기
        val obj = request.getAttribute("errorResponse")
        if (obj is ErrorResponse) {
            println("ErrorResponse found in request attributes")
            println("ErrorResponse object: $obj")
        }
        
        // Exception이 있는 경우 로깅
        if (ex != null) {
            println("Exception occurred: ${ex.message}")
        }
    }
} 