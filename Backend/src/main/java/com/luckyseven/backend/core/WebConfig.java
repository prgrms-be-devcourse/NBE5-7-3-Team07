package com.luckyseven.backend.core;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(new LoggingInterceptor());
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource(){
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.addAllowedOrigin("http://localhost:3000");//허용할 Origin
    configuration.addAllowedMethod("*");//모든 HTTP 메서드 허용
    configuration.addAllowedHeader("*");// 모든 헤더 허용
    configuration.addExposedHeader("Authorization");
    configuration.setAllowCredentials(true);// 인증 정보 허용

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**",configuration);
    return source;
  }



}
