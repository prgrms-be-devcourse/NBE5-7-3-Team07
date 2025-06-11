package com.luckyseven.backend.domain.team.service;

import com.luckyseven.backend.domain.member.entity.Member;
import com.luckyseven.backend.domain.member.repository.MemberRepository;
import com.luckyseven.backend.sharedkernel.exception.CustomLogicException;
import com.luckyseven.backend.sharedkernel.exception.ExceptionCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TempMemberService {

  private final MemberRepository memberRepository;

  public Member getCurrentMember() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    Long memberId;

    if (authentication.getPrincipal() instanceof Long) {
      memberId = (Long) authentication.getPrincipal();
    } else {
      memberId = Long.parseLong(authentication.getName());
    }

    return memberRepository.findById(memberId)
        .orElseThrow(() -> new CustomLogicException(ExceptionCode.USER_NOT_FOUND,
            "ID가 [%d]인 회원을 찾을 수 없습니다", memberId));

  }
}