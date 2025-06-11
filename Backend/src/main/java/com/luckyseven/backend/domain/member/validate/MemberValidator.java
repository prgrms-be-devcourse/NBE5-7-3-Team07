package com.luckyseven.backend.domain.member.validate;

import com.luckyseven.backend.domain.member.dto.RegisterMemberRequest;
import com.luckyseven.backend.domain.member.repository.MemberRepository;
import com.luckyseven.backend.sharedkernel.exception.CustomLogicException;
import com.luckyseven.backend.sharedkernel.exception.ExceptionCode;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MemberValidator {
  private final MemberRepository memberRepository;
  private final Validator validator;

  public void registerRequestValidator(RegisterMemberRequest req) {
    Set<ConstraintViolation<RegisterMemberRequest>> violations
        = validator.validate(req);
    if (!violations.isEmpty()) {
      // 첫 번째 위반만 처리
      ConstraintViolation<RegisterMemberRequest> v = violations.iterator().next();
      String field      = v.getPropertyPath().toString();
      String message    = v.getMessage();

      switch (field) {
        case "email":
          throw new CustomLogicException(
              ExceptionCode.INVALID_EMAIL_FORMAT, message);
        case "password":
          throw new CustomLogicException(
              ExceptionCode.INVALID_PASSWORD_FORMAT, message);
        case "checkPassword":
          throw new CustomLogicException(
              ExceptionCode.INVALID_CHECKPASSWORD_FORMAT, message);
        default:
          throw new CustomLogicException(
              ExceptionCode.BAD_REQUEST, message);
      }
    }
  }

  public void checkEqualsPassword(String password , String checkPassword){
    if(!password.equals(checkPassword)){
      throw new CustomLogicException(ExceptionCode.MEMBER_PASSWORD_MISMATCH);
    }
  }

  public void checkDuplicateEmail(String email){
    if(memberRepository.findByEmail(email).isPresent()){
      throw new CustomLogicException(ExceptionCode.MEMBER_EMAIL_DUPLICATE,(Object) email);
    }
  }

  public void checkDuplicateNicName(String nickname){
    if(memberRepository.findByNickname(nickname).isPresent()) {
      throw new CustomLogicException(ExceptionCode.MEMBER_NICKNAME_DUPLICATE,(Object) nickname);
    }
  }


}
