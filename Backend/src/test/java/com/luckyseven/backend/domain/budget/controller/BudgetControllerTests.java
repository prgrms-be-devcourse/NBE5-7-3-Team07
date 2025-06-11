package com.luckyseven.backend.domain.budget.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.luckyseven.backend.domain.budget.dto.BudgetCreateRequest;
import com.luckyseven.backend.domain.budget.dto.BudgetCreateResponse;
import com.luckyseven.backend.domain.budget.dto.BudgetReadResponse;
import com.luckyseven.backend.domain.budget.dto.BudgetUpdateRequest;
import com.luckyseven.backend.domain.budget.dto.BudgetUpdateResponse;
import com.luckyseven.backend.domain.budget.entity.CurrencyCode;
import com.luckyseven.backend.domain.budget.service.BudgetService;
import com.luckyseven.backend.domain.budget.validator.BudgetValidator;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class BudgetControllerTests {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockBean
  private BudgetService budgetService;

  @MockBean
  private BudgetValidator budgetValidator;

  @Test
  @DisplayName("POST /api/teams/{teamId}/budget - 예산 생성 성공 테스트")
  void create_should_return_201() throws Exception {

    // given
    Long teamId = 1L;
    BudgetCreateRequest request = new BudgetCreateRequest(BigDecimal.valueOf(100000),
        true, BigDecimal.valueOf(1393.7), CurrencyCode.USD);
    BudgetCreateResponse response = new BudgetCreateResponse(1L, LocalDateTime.now(),
        1L, BigDecimal.valueOf(100000), BigDecimal.valueOf(1393.7),
        BigDecimal.valueOf(71.75));
    when(budgetService.save(teamId, 2L, request)).thenReturn(response);

    // when
    mockMvc.perform(post("/api/teams/{teamId}/budget", teamId)
            .contentType(MediaType.APPLICATION_JSON)
            .param("loginMemberId", "2")
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(1L))
        .andReturn();

    // then
    verify(budgetValidator).validateRequest(request);

  }

  @Test
  @DisplayName("GET /api/teams/{teamId}/budget - 예산 조회 성공 테스트")
  void read_should_return_200() throws Exception {

    // given
    Long teamId = 1L;
    BudgetReadResponse response = new BudgetReadResponse(1L, LocalDateTime.now(), 1L,
        BigDecimal.valueOf(100000), BigDecimal.valueOf(100000), CurrencyCode.USD,
        BigDecimal.valueOf(1393.70), BigDecimal.valueOf(71.75));


    when(budgetService.getByTeamId(teamId)).thenReturn(response);

    // when & then
    mockMvc.perform(get("/api/teams/{teamId}/budget", teamId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1L))
            .andExpect(jsonPath("$.totalAmount").value(100000))
            .andExpect(jsonPath("$.balance").value(100000));

  }

  @Test
  @DisplayName("PATCH /api/teams/{teamId}/budget - 예산 수정 성공 테스트")
  void patch_should_return_200() throws Exception {

    // given
    Long teamId = 1L;
    BudgetUpdateRequest request = new BudgetUpdateRequest(BigDecimal.valueOf(150000),
        false, null, null);

    BudgetUpdateResponse response = new BudgetUpdateResponse(1L, LocalDateTime.now(),
        1L, BigDecimal.valueOf(150000), CurrencyCode.USD, BigDecimal.valueOf(1393.7),
        BigDecimal.valueOf(107.63));

    when(budgetService.updateByTeamId(teamId, 2L, request)).thenReturn(response);

    // when & then
    mockMvc.perform(patch("/api/teams/{teamId}/budget", teamId)
          .contentType(MediaType.APPLICATION_JSON)
            .param("loginMemberId", "2")
          .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1L))
        .andExpect(jsonPath("$.balance").value(BigDecimal.valueOf(150000)));

  }

  @Test
  @DisplayName("DELETE /api/teams/{teamId}/budget - 예산 삭제 성공 테스트")
  void delete_should_return_204() throws Exception {

    // given
    Long teamId = 1L;

    // when & then
    mockMvc.perform(delete("/api/teams/{teamId}/budget", teamId))
        .andExpect(status().isNoContent());

    verify(budgetService).deleteByTeamId(teamId);

  }
}