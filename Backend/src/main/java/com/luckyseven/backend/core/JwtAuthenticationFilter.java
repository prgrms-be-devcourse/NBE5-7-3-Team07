package com.luckyseven.backend.core;

import com.luckyseven.backend.domain.member.service.utill.MemberDetails;
import com.luckyseven.backend.sharedkernel.exception.CustomLogicException;
import com.luckyseven.backend.sharedkernel.exception.ExceptionCode;
import com.luckyseven.backend.sharedkernel.jwt.utill.JwtTokenizer;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.PathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
  private final JwtTokenizer jwtTokenizer;
  private final CustomAuthenticationEntryPoint authenticationEntryPoint;
  private final PathMatcher pathMatcher;

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request){

    String path = request.getRequestURI();
    return path.startsWith("/api/users/") || path.startsWith("/api/email/");
  }


  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {
    String accessToken = resolveToken(request);
    try{
      if(StringUtils.hasText(accessToken)){
        Claims claims = jwtTokenizer.parseAccessToken(accessToken);
        setAuthenticationToContext(claims);
        log.info("[JwtAuthenticationFilter] doFilterInternal = {}", claims);

      }else{
        throw new CustomLogicException(ExceptionCode.JWT_TOKEN_NOT_FOUND);
      }
    }catch(AuthenticationException ex){
      SecurityContextHolder.clearContext();
      authenticationEntryPoint.commence(request,response,ex);
      return;
    }
    filterChain.doFilter(request,response);

  }

  private void setAuthenticationToContext(Claims claims){
    Long memberId = Long.parseLong(claims.getSubject());
    String email =  claims.get("email").toString();
    String nickName= claims.get("nickname").toString();

    MemberDetails userDetails = new MemberDetails(memberId," ",email, nickName);

    log.info("userDetails UserDetails.memberId ={} , UserDetails.email ={}, nickName ={}",memberId,email,nickName);

    Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    SecurityContextHolder.getContext().setAuthentication(authentication);
  }

  private String resolveToken(HttpServletRequest request) {
    String bearerToken = request.getHeader("Authorization");

    if( bearerToken != null && bearerToken.startsWith("Bearer ")) {
      return bearerToken.substring(7);
    }
    return null;
  }
}
