package com.luckyseven.backend.domain.member.service;

import com.luckyseven.backend.domain.member.dto.LoginMemberRequest;
import com.luckyseven.backend.domain.member.dto.RegisterMemberRequest;
import com.luckyseven.backend.domain.member.entity.Member;
import com.luckyseven.backend.domain.member.repository.MemberRepository;
import com.luckyseven.backend.domain.member.service.utill.MemberDetails;
import com.luckyseven.backend.domain.member.validate.MemberValidator;
import com.luckyseven.backend.sharedkernel.exception.CustomLogicException;
import com.luckyseven.backend.sharedkernel.exception.ExceptionCode;
import com.luckyseven.backend.sharedkernel.jwt.entity.BlackListToken;
import com.luckyseven.backend.sharedkernel.jwt.repository.BlackListTokenRepository;
import com.luckyseven.backend.sharedkernel.jwt.repository.RefreshTokenRepository;
import com.luckyseven.backend.sharedkernel.jwt.utill.JwtTokenizer;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import java.util.HashSet;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class MemberService {

  private final MemberRepository memberRepository;
  private final JwtTokenizer jwtTokenizer;
  private final BlackListTokenRepository blackListTokenRepository;
  private final AuthenticationManager authenticationManager;
  private final MemberValidator memberValidator;


  public void checkDuplicateNickName(String nickname) {
    memberValidator.checkDuplicateNicName(nickname);
  }

  public void checkDuplicateEmail(String email) {
    memberValidator.checkDuplicateEmail(email);
  }

  public void checkEqualsPassword(String password, String checkPassword) {
    memberValidator.checkEqualsPassword(password, checkPassword);
  }

  public String registerMember(RegisterMemberRequest req, PasswordEncoder passwordEncoder) {
    memberValidator.registerRequestValidator(req);
    checkDuplicateEmail(req.email());
    checkDuplicateNickName(req.nickname());
    checkEqualsPassword(req.password(), req.checkPassword());

    //TODO : {Mapper} : 설정
    Member newMember = Member.builder()
        .email(req.email())
        .password(passwordEncoder.encode(req.password()))
        .nickname(req.nickname())
        .build();
    memberRepository.save(newMember);
    return req.email();
  }

  public void logout(
      String refreshToken,
      HttpServletResponse resp
  ) {
    jwtTokenizer.LogoutRefreshToken(refreshToken);

    //TODO: 올바른 삭제 방법인가?
    Cookie expired = new Cookie("refreshToken", null);
    expired.setPath("/");
    expired.setMaxAge(0);
    resp.addCookie(expired);
  }

  public String Login(LoginMemberRequest req, HttpServletResponse resp) {
    UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(req.email(),
        req.password());
    Authentication auth = authenticationManager.authenticate(token);
    MemberDetails memberDetails = (MemberDetails) auth.getPrincipal();
    return jwtTokenizer.reissueTokenPair(resp, memberDetails);
  }

  public Member findMemberOrThrow(Long id) {
    return memberRepository.findById(id).orElseThrow(
        () -> new CustomLogicException(ExceptionCode.MEMBER_ID_NOTFOUND, id)
    );
  }

  public List<Member> findMembersByIds(List<Long> ids) {
    List<Member> members = memberRepository.findAllById(ids);
    if (members.size() != new HashSet<>(ids).size()) {
      throw new CustomLogicException(ExceptionCode.MEMBER_ID_NOTFOUND);
    }
    return members;
  }
}
