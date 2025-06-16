package com.luckyseven.backend.domain.budget.controller;

import static com.luckyseven.backend.domain.budget.util.TestUtils.genAuthentication;
import static com.luckyseven.backend.domain.budget.util.TestUtils.genBudgetAddReq;
import static com.luckyseven.backend.domain.budget.util.TestUtils.genBudgetCreateReq;
import static com.luckyseven.backend.domain.budget.util.TestUtils.genBudgetCreateResp;
import static com.luckyseven.backend.domain.budget.util.TestUtils.genBudgetReadResp;
import static com.luckyseven.backend.domain.budget.util.TestUtils.genBudgetUpdateReq;
import static com.luckyseven.backend.domain.budget.util.TestUtils.genBudgetUpdateResp;
import static com.luckyseven.backend.domain.budget.util.TestUtils.genBudgetUpdateRespAfterAdd;
import static com.luckyseven.backend.domain.budget.util.TestUtils.genMemberDetails;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.luckyseven.backend.domain.budget.dto.BudgetAddRequest;
import com.luckyseven.backend.domain.budget.dto.BudgetCreateRequest;
import com.luckyseven.backend.domain.budget.dto.BudgetCreateResponse;
import com.luckyseven.backend.domain.budget.dto.BudgetReadResponse;
import com.luckyseven.backend.domain.budget.dto.BudgetUpdateRequest;
import com.luckyseven.backend.domain.budget.dto.BudgetUpdateResponse;
import com.luckyseven.backend.domain.budget.entity.CurrencyCode;
import com.luckyseven.backend.domain.budget.service.BudgetService;
import com.luckyseven.backend.domain.budget.validator.BudgetValidator;
import com.luckyseven.backend.domain.member.service.utill.MemberDetails;
import com.luckyseven.backend.sharedkernel.exception.CustomLogicException;
import com.luckyseven.backend.sharedkernel.exception.ExceptionCode;
import java.math.BigDecimal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class BudgetControllerTests {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private BudgetService budgetService;

  @MockitoBean
  private BudgetValidator budgetValidator;

  // SecurityContext 정리
  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  @DisplayName("POST /api/teams/{teamId}/budgets로 요청해 예산 생성이 성공하면 201 CREATED를 응답한다")
  void create_should_return_201() throws Exception {

    MemberDetails member = genMemberDetails();
    genAuthentication(member);

    Long teamId = 1L;
    BudgetCreateRequest req = genBudgetCreateReq();
    BudgetCreateResponse resp = genBudgetCreateResp();

    when(budgetService.save(teamId, member.getId(), req)).thenReturn(resp);

    mockMvc.perform(
            post("/api/teams/{teamId}/budgets", teamId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
        .andExpect(status().isCreated())
        .andDo(print());

    verify(budgetValidator, times(1)).validateRequest(req);
  }

  @Test
  @DisplayName("totalAmount가 null인 요청을 받으면 400 BAD REQUEST와 함께 예산 생성에 실패한다")
  void create_should_return_400() throws Exception {

    MemberDetails member = genMemberDetails();
    genAuthentication(member);

    Long teamId = 1L;
    BudgetCreateRequest req = new BudgetCreateRequest(
        null,
        true,
        BigDecimal.valueOf(1393.7),
        CurrencyCode.USD
    );

    when(budgetService.save(teamId, member.getId(), req))
        .thenThrow(new CustomLogicException(ExceptionCode.BAD_REQUEST));

    mockMvc.perform(
            post("/api/teams/{teamId}/budgets", teamId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))
        )
        .andExpect(status().isBadRequest())
        .andDo(print());

  }

  @Test
  @DisplayName("GET /api/teams/{teamId}/budgets 을 요청해 팀 예산 조회에 성공하면 200 OK로 응답한다")
  void read_should_return_200() throws Exception {

    Long teamId = 1L;
    BudgetReadResponse resp = genBudgetReadResp();

    when(budgetService.getByTeamId(teamId)).thenReturn(resp);

    mockMvc.perform(
            get("/api/teams/{teamId}/budgets", teamId)
        )
        .andExpect(status().isOk())
        .andDo(print());
  }

  @Test
  @DisplayName("존재하지 않는 teamId로 예산 조회를 할 경우 404 NOT FOUND가 발생한다")
  void read_should_return_404() throws Exception {

    Long teamId = 1L;
    when(budgetService.getByTeamId(teamId))
        .thenThrow(new CustomLogicException(ExceptionCode.TEAM_NOT_FOUND));

    mockMvc.perform(
            get("/api/teams/{teamId}/budgets", teamId)
        )
        .andExpect(status().isNotFound())
        .andDo(print());

  }

  @Test
  @DisplayName("DELETE /api/teams/{teamId}/budgets 를 요청해 예산 삭제에 성공하면 204 NO CONTENT를 응답한다")
  void delete_should_return_204() throws Exception {

    Long teamId = 1L;

    mockMvc.perform(
            delete("/api/teams/{teamId}/budgets", teamId)
        )
        .andExpect(status().isNoContent())
        .andDo(print());

    verify(budgetService, times(1)).deleteByTeamId(teamId);

  }

  @Test
  @DisplayName("PATCH /api/teams/{teamId}/budgets를 요청해 예산 수정에 성공하면 200 OK를 응답한다")
  void update_should_return_200() throws Exception {

    Long teamId = 1L;

    MemberDetails member = genMemberDetails();
    genAuthentication(member);

    BudgetUpdateRequest req = genBudgetUpdateReq();
    BudgetUpdateResponse resp = genBudgetUpdateResp();

    when(budgetService.updateByTeamId(teamId, member.getId(), req)).thenReturn(resp);

    mockMvc.perform(
            patch("/api/teams/{teamId}/budgets", teamId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))
        )
        .andExpect(status().isOk())
        .andDo(print());

    verify(budgetValidator, times(1)).validateRequest(req);
  }

  @Test
  @DisplayName("totalAmount를 null로 요청한 경우 예산 수정에 실패하고 400 BAD REQUEST를 응답한다")
  void update_should_return_400() throws Exception {

    Long teamId = 1L;

    MemberDetails member = genMemberDetails();
    genAuthentication(member);

    BudgetUpdateRequest req = new BudgetUpdateRequest(
        null,
        true,
        BigDecimal.valueOf(1393.7)
    );

    when(budgetService.updateByTeamId(teamId, member.getId(), req))
        .thenThrow(new CustomLogicException(ExceptionCode.BAD_REQUEST));

    mockMvc.perform(
            patch("/api/teams/{teamId}/budgets", teamId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))
        )
        .andExpect(status().isBadRequest())
        .andDo(print());

  }

  @Test
  @DisplayName("PATCH /api/teams/{teamId}/budgets/add를 요청해 예산 추가에 성공하면 200 OK를 응답한다")
  void add_should_return_200() throws Exception {

    Long teamId = 1L;

    MemberDetails member = genMemberDetails();
    genAuthentication(member);

    BudgetAddRequest req = genBudgetAddReq();
    BudgetUpdateResponse resp = genBudgetUpdateRespAfterAdd();

    when(budgetService.addBudgetByTeamId(teamId, member.getId(), req)).thenReturn(resp);

    mockMvc.perform(
            patch("/api/teams/{teamId}/budgets/add", teamId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))
        )
        .andExpect(status().isOk())
        .andDo(print());

    verify(budgetValidator, times(1)).validateRequest(req);
  }

  @Test
  @DisplayName("additionalBudget을 null로 요청한 경우 예산 수정에 실패하고 400 BAD REQUEST를 응답한다")
  void add_should_return_400() throws Exception {

    Long teamId = 1L;

    MemberDetails member = genMemberDetails();
    genAuthentication(member);

    BudgetAddRequest req = new BudgetAddRequest(
        null,
        false,
        null
    );

    when(budgetService.addBudgetByTeamId(teamId, member.getId(), req))
        .thenThrow(new CustomLogicException(ExceptionCode.BAD_REQUEST));

    mockMvc.perform(
            patch("/api/teams/{teamId}/budgets/add", teamId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))
        )
        .andExpect(status().isBadRequest())
        .andDo(print());

  }

}