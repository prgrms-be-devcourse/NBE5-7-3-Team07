package com.luckyseven.backend.domain.expense.controller;

import com.luckyseven.backend.domain.expense.dto.CreateExpenseResponse;
import com.luckyseven.backend.domain.expense.dto.ExpenseBalanceResponse;
import com.luckyseven.backend.domain.expense.dto.ExpenseRequest;
import com.luckyseven.backend.domain.expense.dto.ExpenseResponse;
import com.luckyseven.backend.domain.expense.dto.ExpenseUpdateRequest;
import com.luckyseven.backend.domain.expense.service.ExpenseService;
import com.luckyseven.backend.sharedkernel.dto.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Tag(name = "Expense", description = "지출 내역 관리 API")
public class ExpenseController {

  private final ExpenseService expenseService;

  @Operation(summary = "지출 내역 등록")
  @ApiResponses({
      @ApiResponse(responseCode = "201", description = "지출 내역 등록 성공"),
      @ApiResponse(responseCode = "400", description = "지출 금액이 예산을 초과"),
      @ApiResponse(responseCode = "404", description = "결제자 또는 팀을 찾을 수 없음"),
  })
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping("/{teamId}/expense")
  public CreateExpenseResponse createExpense(
      @PathVariable @Positive Long teamId,
      @RequestBody @Valid ExpenseRequest request
  ) {
    return expenseService.saveExpense(teamId, request);
  }

  @Operation(summary = "지출 내역 수정")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "지출 내역 수정 성공"),
      @ApiResponse(responseCode = "400", description = "지출 금액이 예산을 초과"),
      @ApiResponse(responseCode = "404", description = "지출 내역을 찾을 수 없음"),
  })
  @ResponseStatus(HttpStatus.OK)
  @PatchMapping("/expense/{expenseId}")
  public CreateExpenseResponse updateExpense(
      @PathVariable Long expenseId,
      @RequestBody @Valid ExpenseUpdateRequest request
  ) {
    return expenseService.updateExpense(expenseId, request);
  }

  @Operation(summary = "지출 내역 삭제")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "지출 내역 삭제 성공"),
      @ApiResponse(responseCode = "404", description = "지출 내역을 찾을 수 없음")
  })
  @ResponseStatus(HttpStatus.OK)
  @DeleteMapping("/expense/{expenseId}")
  public ExpenseBalanceResponse deleteExpense(@PathVariable Long expenseId) {
    return expenseService.deleteExpense(expenseId);
  }

  @Operation(summary = "지출 내역 상세 조회")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "지출 내역 상세 조회 성공"),
      @ApiResponse(responseCode = "404", description = "지출 내역을 찾을 수 없음")
  })
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/expense/{expenseId}")
  public ExpenseResponse getExpense(@PathVariable Long expenseId) {
    return expenseService.getExpense(expenseId);
  }

  @Operation(summary = "지출 내역 목록 조회")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "지출 내역 목록 조회 성공"),
      @ApiResponse(responseCode = "404", description = "팀을 찾을 수 없음")
  })
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/{teamId}/expenses")
  public PageResponse<ExpenseResponse> getListExpense(
      @PathVariable @Positive Long teamId,
      @ParameterObject
      @PageableDefault(
          sort = "createdAt",
          direction = Sort.Direction.DESC
      )
      Pageable pageable
  ) {
    return expenseService.getListExpense(teamId, pageable);
  }
}
