package com.luckyseven.backend.sharedkernel.jwt.Controller;


import com.luckyseven.backend.sharedkernel.jwt.utill.JwtTokenizer;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class RefreshTokenController {
  private final JwtTokenizer jwtTokenizer;
  @PostMapping("api/refresh")
  public ResponseEntity<Void> refreshToken(@CookieValue(name = "refreshToken") String token,HttpServletResponse response) {
    String accessToken = jwtTokenizer.validateRefreshToken(token,response);
    return ResponseEntity.ok().header("Authorization", "Bearer " + accessToken).build();
  }
}
