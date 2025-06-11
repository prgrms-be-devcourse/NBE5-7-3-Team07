package com.luckyseven.backend.domain.settlements.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.luckyseven.backend.core.JwtAuthenticationFilter;
import com.luckyseven.backend.domain.settlement.api.SettlementController;
import com.luckyseven.backend.domain.settlement.app.SettlementService;
import com.luckyseven.backend.domain.settlement.dto.SettlementResponse;
import com.luckyseven.backend.domain.settlement.dto.SettlementSearchCondition;
import com.luckyseven.backend.domain.settlement.dto.SettlementUpdateRequest;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@WebMvcTest(controllers = SettlementController.class, excludeFilters = {
    @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class)
})
@AutoConfigureMockMvc(addFilters = false)
class SettlementControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private SettlementService settlementService;

  @MockitoBean
  private JpaMetamodelMappingContext jpaMetamodelMappingContext;


  @Test
  @DisplayName("ID로 정산 조회 API 테스트")
  void readSettlement_ShouldReturnSettlement() throws Exception {
    // given
    Long settlementId = 1L;
    SettlementResponse mockResponse = SettlementResponse.builder()
        .id(settlementId)
        .settlerId(10L)
        .payerId(20L)
        .expenseId(30L)
        .build();

    when(settlementService.readSettlement(settlementId)).thenReturn(mockResponse);

    // when
    MvcResult result = mockMvc.perform(get("/api/settlements/{settlementId}", settlementId)
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(settlementId))
        .andReturn();

    // then

    verify(settlementService, times(1)).readSettlement(settlementId);
  }

  @Test
  @DisplayName("팀ID로 정산 목록 조회 API 테스트")
  void readSettlements_ShouldReturnSettlementPage() throws Exception {
    // given
    Long teamId = 1L;
    SettlementResponse settlement1 = SettlementResponse.builder()
        .id(1L)
        .amount(BigDecimal.valueOf(1000))
        .settlerId(10L)
        .payerId(20L)
        .expenseId(30L)
        .build();

    SettlementResponse settlement2 = SettlementResponse.builder()
        .id(2L)
        .amount(BigDecimal.valueOf(2000))
        .settlerId(11L)
        .payerId(21L)
        .expenseId(31L)
        .isSettled(true)
        .build();

    List<SettlementResponse> settlements = List.of(settlement1, settlement2);
    Page<SettlementResponse> mockPage = new PageImpl<>(settlements, PageRequest.of(0, 10), 2);

    when(settlementService.readSettlementPage(eq(teamId), any(SettlementSearchCondition.class),
        any(Pageable.class)))
        .thenReturn(mockPage);

    // when
    MvcResult result = mockMvc.perform(get("/api/teams/{teamId}/settlements", teamId)
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].id").value(settlement1.id))
        .andExpect(jsonPath("$.content[1].id").value(settlement2.id))
        .andReturn();

    // then

    verify(settlementService, times(1)).readSettlementPage(eq(teamId),
        any(SettlementSearchCondition.class), any(Pageable.class));
  }

  @Test
  @DisplayName("정산 목록 조회 API - 정렬 파라미터 테스트")
  void readSettlements_WithSortParameter_ShouldReturnSortedPage() throws Exception {
    // given
    Long teamId = 1L;
    SettlementResponse settlement1 = SettlementResponse.builder()
        .id(1L)
        .amount(BigDecimal.valueOf(2000))
        .build();

    SettlementResponse settlement2 = SettlementResponse.builder()
        .id(2L)
        .amount(BigDecimal.valueOf(1000))
        .build();

    // 금액 내림차순 정렬 결과 (2000원이 먼저 나옴)
    List<SettlementResponse> settlements = List.of(settlement1, settlement2);
    Page<SettlementResponse> mockPage = new PageImpl<>(settlements, PageRequest.of(0, 10), 2);

    when(settlementService.readSettlementPage(eq(teamId), any(SettlementSearchCondition.class),
        any(Pageable.class)))
        .thenReturn(mockPage);

    // when
    MvcResult result = mockMvc.perform(get("/api/teams/{teamId}/settlements", teamId)
            .param("sort", "amount,desc")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andReturn();

    // then
    String content = result.getResponse().getContentAsString();

    // ID 순서로 확인 (정렬된 결과대로 나오는지)
    int firstIdIndex = content.indexOf("\"id\":1");
    int secondIdIndex = content.indexOf("\"id\":2");
    assertThat(firstIdIndex).isLessThan(secondIdIndex);

    verify(settlementService, times(1)).readSettlementPage(eq(teamId),
        any(SettlementSearchCondition.class), any(Pageable.class));
  }

  @Test
  @DisplayName("정산 수정 API 테스트")
  void updateSettlement_ShouldUpdateAndReturnSettlement() throws Exception {
    // given
    Long settlementId = 1L;
    SettlementUpdateRequest request = SettlementUpdateRequest.builder()
        .amount(BigDecimal.valueOf(2000))
        .payerId(20L)
        .settlerId(10L)
        .expenseId(30L)
        .build();

    SettlementResponse mockResponse = SettlementResponse.builder()
        .id(settlementId)
        .amount(BigDecimal.valueOf(2000))
        .settlerId(10L)
        .payerId(20L)
        .expenseId(30L)
        .isSettled(false)
        .build();

    when(settlementService.updateSettlement(eq(settlementId), any(SettlementUpdateRequest.class)))
        .thenReturn(mockResponse);

    // when
    MvcResult result = mockMvc.perform(patch("/api/settlements/{settlementId}", settlementId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andReturn();

    // then
    String content = result.getResponse().getContentAsString();
    SettlementResponse response = objectMapper.readValue(content, SettlementResponse.class);

    assertThat(response.id).isEqualTo(settlementId);
    assertThat(response.amount).isEqualTo(BigDecimal.valueOf(2000));
    assertThat(response.settlerId()).isEqualTo(10L);
    assertThat(response.payerId).isEqualTo(20L);
    assertThat(response.expenseId).isEqualTo(30L);
    assertThat(response.isSettled).isFalse();

    verify(settlementService, times(1)).updateSettlement(eq(settlementId),
        any(SettlementUpdateRequest.class));
  }

  @Test
  @DisplayName("정산 완료 처리 API 테스트")
  void settleSettlement_ShouldMarkAsSettled() throws Exception {
    // given
    Long settlementId = 1L;
    SettlementUpdateRequest request = SettlementUpdateRequest.builder()
        .amount(BigDecimal.valueOf(1000))
        .payerId(20L)
        .settlerId(10L)
        .expenseId(30L)
        .build();

    SettlementResponse mockResponse = SettlementResponse.builder()
        .id(settlementId)
        .amount(BigDecimal.valueOf(1000))
        .settlerId(10L)
        .payerId(20L)
        .expenseId(30L)
        .isSettled(true)
        .build();

    when(settlementService.settleSettlement(settlementId)).thenReturn(mockResponse);

    // when
    MvcResult result = mockMvc.perform(patch("/api/settlements/{settlementId}", settlementId)
            .param("settledOnly", "true")  // settledOnly=true 파라미터 추가
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andReturn();

    // then
    String content = result.getResponse().getContentAsString();
    SettlementResponse response = objectMapper.readValue(content, SettlementResponse.class);

    assertThat(response.id).isEqualTo(settlementId);
    assertThat(response.isSettled).isTrue(); // 정산 완료됨 확인

    verify(settlementService, times(1)).settleSettlement(settlementId);
    verify(settlementService, times(0)).updateSettlement(anyLong(),
        any()); // updateSettlement는 호출되지 않음
  }
}