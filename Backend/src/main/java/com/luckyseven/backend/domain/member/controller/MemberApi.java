package com.luckyseven.backend.domain.member.controller;



import com.luckyseven.backend.domain.member.dto.RegisterMemberRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;




//TODO : 오류 부분도 확인
@Tag(name = "Member", description = "Member관련 API")
public interface MemberApi {
  @Operation(summary = "회원가입")
  @ApiResponse(
      responseCode = "201",
      description = "회원가입 성공",
      content = @Content(schema = @Schema(implementation = String.class)))
  ResponseEntity<String> registerMember(
      @Parameter RegisterMemberRequest req
  );

  @Operation(summary = "이메일 중복")
  @ApiResponse(
      responseCode = "204",
      description = "이메일 중복 통과"
  )
  ResponseEntity<Void> checkEmail(
      @Parameter String email
  );

  @Operation(summary = "닉네임 중복")
  @ApiResponse(
      responseCode = "204",
      description = "닉네임 중복 통과"
  )
  ResponseEntity<Void> checkNickName(
      @Parameter String nickname
  );

  @Operation(summary = "비밀번호가 맞는지 확인")
  @ApiResponse(
      responseCode = "204",
      description = "비밀번호 동일 통과"
  )
  ResponseEntity<Void> checkPassword(
      @Parameter String password,
      @Parameter String checkPassword
  );

  @Operation(summary = "로그아웃기능")
  @ApiResponse(
      responseCode = "200",
      description = "로그아웃"
  )
  ResponseEntity<Void> logout(
      @Parameter(description = "refreshToken 받아오기") String refreshToken,
      HttpServletResponse resp
  );

}
