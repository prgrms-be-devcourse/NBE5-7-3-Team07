package com.luckyseven.backend.domain.budget.controller

import com.luckyseven.backend.domain.budget.dto.*
import com.luckyseven.backend.domain.budget.service.BudgetService
import com.luckyseven.backend.domain.budget.validator.BudgetValidator
import com.luckyseven.backend.domain.member.service.utill.MemberDetails
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import jakarta.validation.constraints.Positive
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@Validated
@RestController
@RequestMapping("/api/teams")
@Tag(name = "Budget", description = "팀별 예산 관리 API")
class BudgetController(
    private val budgetService: BudgetService,
    private val budgetValidator: BudgetValidator
) {

    @Operation(summary = "팀 예산 생성")
    @ApiResponse(responseCode = "201", description = "예산 생성 성공")
    @ApiResponse(responseCode = "400", description = "예산 생성 실패")
    @ApiResponse(responseCode = "409", description = "예산 중복 생성 시도로 인한 실패")
    @PostMapping("/{teamId}/budgets")
    @ResponseStatus(HttpStatus.CREATED)
    fun create(
        @PathVariable @Positive teamId: Long,
        @AuthenticationPrincipal memberDetails: MemberDetails,
        @Valid @RequestBody request: BudgetCreateRequest
    ): BudgetCreateResponse {
        budgetValidator.validateRequest(request)
        return budgetService.save(teamId, memberDetails.id, request)
    }

    @Operation(summary = "팀 예산 조회")
    @ApiResponse(responseCode = "200", description = "예산 조회 성공")
    @ApiResponse(responseCode = "404", description = "예산 정보를 찾을 수 없음")
    @GetMapping("/{teamId}/budgets")
    @ResponseStatus(HttpStatus.OK)
    fun read(@PathVariable @Positive teamId: Long): BudgetReadResponse {
        return budgetService.getByTeamId(teamId)
    }

    @Operation(summary = "팀의 총 예산, 환전 여부, 환율을 수정")
    @ApiResponse(responseCode = "200", description = "예산 수정 성공")
    @ApiResponse(responseCode = "400", description = "예산 수정 실패")
    @ApiResponse(responseCode = "404", description = "예산 정보를 찾을 수 없음")
    @PatchMapping("/{teamId}/budgets")
    @ResponseStatus(HttpStatus.OK)
    fun update(
        @PathVariable @Positive teamId: Long,
        @AuthenticationPrincipal memberDetails: MemberDetails,
        @Valid @RequestBody request: BudgetUpdateRequest
    ): BudgetUpdateResponse {
        budgetValidator.validateRequest(request)
        return budgetService.updateByTeamId(teamId, memberDetails.id, request)
    }

    @Operation(summary = "팀 예산 추가")
    @ApiResponse(responseCode = "200", description = "예산 추가 성공")
    @ApiResponse(responseCode = "400", description = "예산 추가 실패")
    @ApiResponse(responseCode = "404", description = "예산 정보를 찾을 수 없음")
    @PatchMapping("/{teamId}/budgets/add")
    @ResponseStatus(HttpStatus.OK)
    fun add(
        @PathVariable @Positive teamId: Long,
        @AuthenticationPrincipal memberDetails: MemberDetails,
        @Valid @RequestBody request: BudgetAddRequest
    ): BudgetUpdateResponse {
        budgetValidator.validateRequest(request)
        return budgetService.addBudgetByTeamId(teamId, memberDetails.id, request)
    }

    @Operation(summary = "팀 예산 삭제")
    @ApiResponse(responseCode = "204", description = "예산 삭제 성공")
    @ApiResponse(responseCode = "404", description = "예산 정보를 찾을 수 없음")
    @DeleteMapping("/{teamId}/budgets")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@PathVariable @Positive teamId: Long) {
        budgetService.deleteByTeamId(teamId)
    }
}
