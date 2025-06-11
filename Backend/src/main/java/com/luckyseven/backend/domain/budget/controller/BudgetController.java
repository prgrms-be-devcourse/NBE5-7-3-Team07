package com.luckyseven.backend.domain.budget.controller;

import com.luckyseven.backend.domain.budget.dto.BudgetCreateRequest;
import com.luckyseven.backend.domain.budget.dto.BudgetCreateResponse;
import com.luckyseven.backend.domain.budget.dto.BudgetReadResponse;
import com.luckyseven.backend.domain.budget.dto.BudgetUpdateRequest;
import com.luckyseven.backend.domain.budget.dto.BudgetUpdateResponse;
import com.luckyseven.backend.domain.budget.service.BudgetService;
import com.luckyseven.backend.domain.budget.validator.BudgetValidator;
import com.luckyseven.backend.domain.member.entity.Member;
import com.luckyseven.backend.domain.member.service.utill.MemberDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/teams")
@RequiredArgsConstructor
@Tag(name = "Budget", description = "팀별 예산 관리 API")
public class BudgetController {

  private final BudgetService budgetService;
  private final BudgetValidator budgetValidator;

  @Operation(summary = "팀 예산 생성")
  @ApiResponse(responseCode = "201", description = "예산 생성 성공")
  @ApiResponse(responseCode = "400", description = "예산 생성 실패")
  @ApiResponse(responseCode = "409", description = "예산 중복 생성 시도로 인한 실패")
  @PostMapping("/{teamId}/budget")
  @ResponseStatus(HttpStatus.CREATED)
  public BudgetCreateResponse create(@PathVariable @Positive Long teamId,
      @AuthenticationPrincipal MemberDetails memberDetails,
      @Valid @RequestBody BudgetCreateRequest request) {
    budgetValidator.validateRequest(request);

    return budgetService.save(teamId, memberDetails.getId(), request);
  }

  @Operation(summary = "팀 예산 조회")
  @ApiResponse(responseCode = "200", description = "예산 조회 성공")
  @ApiResponse(responseCode = "404", description = "예산 정보를 찾을 수 없음")
  @GetMapping("/{teamId}/budget")
  @ResponseStatus(HttpStatus.OK)
  public BudgetReadResponse read(@PathVariable @Positive Long teamId) {
    return budgetService.getByTeamId(teamId);
  }

  @Operation(summary = "팀의 총 예산, 환전 여부, 환율을 부분 혹은 전체 수정")
  @ApiResponse(responseCode = "200", description = "예산 수정 성공")
  @ApiResponse(responseCode = "400", description = "예산 수정 실패")
  @ApiResponse(responseCode = "404", description = "예산 정보를 찾을 수 없음")
  @PatchMapping("/{teamId}/budget")
  @ResponseStatus(HttpStatus.OK)
  public BudgetUpdateResponse update(@PathVariable @Positive Long teamId,
      @AuthenticationPrincipal MemberDetails memberDetails,
      @Valid @RequestBody BudgetUpdateRequest request) {
    budgetValidator.validateRequest(request);

    return budgetService.updateByTeamId(teamId, memberDetails.getId(), request);
  }

  @Operation(summary = "팀 예산 삭제")
  @ApiResponse(responseCode = "204", description = "예산 삭제 성공")
  @ApiResponse(responseCode = "404", description = "예산 정보를 찾을 수 없음")
  @DeleteMapping("/{teamId}/budget")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable @Positive Long teamId) {
    budgetService.deleteByTeamId(teamId);
  }
}
