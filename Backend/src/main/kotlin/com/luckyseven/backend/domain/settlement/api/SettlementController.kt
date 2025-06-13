package com.luckyseven.backend.domain.settlement.api

import com.luckyseven.backend.domain.settlement.app.SettlementService
import com.luckyseven.backend.domain.settlement.dto.SettlementResponse
import com.luckyseven.backend.domain.settlement.dto.SettlementSearchCondition
import com.luckyseven.backend.domain.settlement.dto.SettlementUpdateRequest
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import jakarta.validation.constraints.Positive
import org.springdoc.core.annotations.ParameterObject
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api")
@Tag(name = "Settlement")
@Validated
class SettlementController(
    private val settlementService: SettlementService
) {

    @Operation(summary = "ID로 정산 조회")
    @ApiResponse(responseCode = "200", description = "Settlement found")
    @ApiResponse(responseCode = "404", description = "Settlement not found")
    @GetMapping("/settlements/{settlementId}")
    @ResponseStatus(HttpStatus.OK)
    fun readSettlement(
        @PathVariable @Positive settlementId: Long
    ): SettlementResponse {
        return settlementService.readSettlement(settlementId)
    }

    @Operation(summary = "팀ID로 정산 목록 조회, 필터조건 추가 가능")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved settlements")
    @GetMapping("/teams/{teamId}/settlements")
    @ResponseStatus(HttpStatus.OK)
    fun readSettlements(
        @PathVariable @Positive teamId: Long,
        @ParameterObject condition: SettlementSearchCondition,
        @PageableDefault(page = 0, size = 10) pageable: Pageable
    ): Page<SettlementResponse> {
        return settlementService.readSettlementPage(teamId, condition, pageable)
    }

    @Operation(summary = "Update settlement")
    @ApiResponse(responseCode = "200", description = "Settlement updated successfully")
    @ApiResponse(responseCode = "404", description = "Settlement not found")
    @PatchMapping("/settlements/{settlementId}")
    @ResponseStatus(HttpStatus.OK)
    fun updateSettlement(
        @PathVariable @Positive settlementId: Long,
        @Parameter(description = "true면 body를 무시하고 정산처리만 진행") @RequestParam(defaultValue = "false") settledOnly: Boolean,
        @Valid @RequestBody request: SettlementUpdateRequest
    ): SettlementResponse {
        return if (settledOnly) {
            settlementService.settleSettlement(settlementId)
        } else {
            settlementService.updateSettlement(settlementId, request)
        }
    }
}
