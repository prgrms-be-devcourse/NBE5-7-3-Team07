package com.luckyseven.backend.domain.expense.controller

import com.luckyseven.backend.domain.expense.dto.*
import com.luckyseven.backend.domain.expense.service.ExpenseService
import com.luckyseven.backend.sharedkernel.dto.PageResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import jakarta.validation.constraints.Positive
import org.springdoc.core.annotations.ParameterObject
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api")
@Tag(name = "Expense", description = "지출 내역 관리 API")
class ExpenseController(
    private val expenseService: ExpenseService
) {

    @Operation(summary = "지출 내역 등록")
    @ApiResponses(
        ApiResponse(responseCode = "201", description = "지출 내역 등록 성공"),
        ApiResponse(responseCode = "400", description = "지출 금액이 예산을 초과"),
        ApiResponse(responseCode = "404", description = "결제자 또는 팀을 찾을 수 없음")
    )
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/{teamId}/expense")
    fun createExpense(
        @PathVariable @Positive teamId: Long,
        @RequestBody @Valid request: ExpenseRequest
    ): CreateExpenseResponse {
        return expenseService.saveExpense(teamId, request)
    }

    @Operation(summary = "지출 내역 수정")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "지출 내역 수정 성공"),
        ApiResponse(responseCode = "400", description = "지출 금액이 예산을 초과"),
        ApiResponse(responseCode = "404", description = "지출 내역을 찾을 수 없음")
    )
    @ResponseStatus(HttpStatus.OK)
    @PatchMapping("/expense/{expenseId}")
    fun updateExpense(
        @PathVariable @Positive expenseId: Long,
        @RequestBody @Valid request: ExpenseUpdateRequest
    ): CreateExpenseResponse {
        return expenseService.updateExpense(expenseId, request)
    }

    @Operation(summary = "지출 내역 삭제")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "지출 내역 삭제 성공"),
        ApiResponse(responseCode = "404", description = "지출 내역을 찾을 수 없음")
    )
    @ResponseStatus(HttpStatus.OK)
    @DeleteMapping("/expense/{expenseId}")
    fun deleteExpense(
        @PathVariable @Positive expenseId: Long
    ): ExpenseBalanceResponse {
        return expenseService.deleteExpense(expenseId)
    }

    @Operation(summary = "지출 내역 상세 조회")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "지출 내역 상세 조회 성공"),
        ApiResponse(responseCode = "404", description = "지출 내역을 찾을 수 없음")
    )
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/expense/{expenseId}")
    fun getExpense(
        @PathVariable @Positive expenseId: Long
    ): ExpenseResponse {
        return expenseService.getExpense(expenseId)
    }

    @Operation(summary = "지출 내역 목록 조회")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "지출 내역 목록 조회 성공"),
        ApiResponse(responseCode = "404", description = "팀을 찾을 수 없음")
    )
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/{teamId}/expenses")
    fun getListExpense(
        @PathVariable @Positive teamId: Long,
        @ParameterObject
        @PageableDefault(
            sort = ["createdAt"],
            direction = Sort.Direction.DESC
        )
        pageable: Pageable
    ): PageResponse<ExpenseResponse> {
        return expenseService.getExpenses(teamId, pageable)
    }
}
