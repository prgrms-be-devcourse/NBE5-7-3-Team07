package com.luckyseven.backend.domain.settlements.api

import com.fasterxml.jackson.databind.ObjectMapper
import com.luckyseven.backend.core.JwtAuthenticationFilter
import com.luckyseven.backend.domain.settlement.api.SettlementController
import com.luckyseven.backend.domain.settlement.app.SettlementService
import com.luckyseven.backend.domain.settlement.dto.SettlementResponse
import com.luckyseven.backend.domain.settlement.dto.SettlementSearchCondition
import com.luckyseven.backend.domain.settlement.dto.SettlementUpdateRequest
import com.ninjasquad.springmockk.MockkBean
import io.kotest.matchers.comparables.shouldBeLessThan
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.FilterType
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.math.BigDecimal
import java.time.LocalDateTime
import kotlin.random.Random

@WebMvcTest(
    controllers = [SettlementController::class],
    excludeFilters = [
        ComponentScan.Filter(
            type = FilterType.ASSIGNABLE_TYPE,
            classes = [JwtAuthenticationFilter::class]
        )
    ]
)
@AutoConfigureMockMvc(addFilters = false)
class SettlementControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockkBean
    private lateinit var settlementService: SettlementService

    @MockkBean
    private lateinit var jpaMetamodelMappingContext: JpaMetamodelMappingContext

    fun genSettlement(
        id: Long = Random.nextLong(),
        amount: Long = Random.nextLong(100000000000000000L),
        settlerId: Long = Random.nextLong(),
        payerId: Long = Random.nextLong(),
        expenseId: Long = Random.nextLong(),
        isSettled: Boolean = Random.nextBoolean(),
        settlerNickName: String = "settler",
        payerNickName: String = "payer",
        expenseDescription: String = "expense",
        teamId: Long = Random.nextLong()
    ): SettlementResponse {
        return SettlementResponse(
            id = id,
            amount = BigDecimal.valueOf(amount),
            settlerId = settlerId,
            payerId = payerId,
            expenseId = expenseId,
            isSettled = isSettled,
            settlerNickName = settlerNickName,
            payerNickName = payerNickName,
            expenseDescription = expenseDescription,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now(),
            teamId = teamId
        )
    }

    @Test
    @DisplayName("ID로 정산 조회 API 테스트")
    fun readSettlement_ShouldReturnSettlement() {
        // given
        val settlementId = 1L
        val mockSettlement = genSettlement(id = settlementId)

        every { settlementService.readSettlement(settlementId) } returns mockSettlement

        // when
        val result = mockMvc.perform(
            get("/api/settlements/${settlementId}")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(settlementId))
            .andReturn()

        // then
        verify(exactly = 1) { settlementService.readSettlement(settlementId) }
    }

    @Test
    @DisplayName("팀ID로 정산 목록 조회 API 테스트")
    fun readSettlements_ShouldReturnSettlementPage() {
        // given
        val teamId = 1L
        val settlement1 = genSettlement(teamId = teamId)

        val settlement2 = genSettlement(teamId = teamId)

        val settlements = listOf(settlement1, settlement2)
        val mockPage = PageImpl(settlements, PageRequest.of(0, 10), 2)

        every {
            settlementService.readSettlementPage(
                eq(teamId),
                any(SettlementSearchCondition::class),
                any(Pageable::class)
            )
        } returns mockPage

        // when
        val result = mockMvc.perform(
            get("/api/teams/${teamId}/settlements")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content[0].id").value(settlement1.id))
            .andExpect(jsonPath("$.content[1].id").value(settlement2.id))
            .andReturn()

        // then
        verify(exactly = 1) {
            settlementService.readSettlementPage(
                eq(teamId),
                any(SettlementSearchCondition::class),
                any(Pageable::class)
            )
        }
    }

    @Test
    @DisplayName("정산 목록 조회 API - 정렬 파라미터 테스트")
    fun readSettlements_WithSortParameter_ShouldReturnSortedPage() {
        // given
        val teamId = 1L
        val settlement1 = genSettlement(teamId = teamId, amount = 2000)

        val settlement2 = genSettlement(teamId = teamId, amount = 1000)

        // 금액 내림차순 정렬 결과 (2000원이 먼저 나옴)
        val settlements = listOf(settlement1, settlement2)
        val mockPage = PageImpl(settlements, PageRequest.of(0, 10), 2)

        every {
            settlementService.readSettlementPage(
                eq(teamId),
                any(SettlementSearchCondition::class),
                any(Pageable::class)
            )
        } returns mockPage

        // when
        val result = mockMvc.perform(
            get("/api/teams/${teamId}/settlements")
                .param("sort", "amount,desc")
        )
            .andExpect(status().isOk)
            .andReturn()

        // then
        val content = result.response.contentAsString

        // ID 순서로 확인 (정렬된 결과대로 나오는지)
        val firstIdIndex = content.indexOf("\"id\":${settlement1.id}")
        val secondIdIndex = content.indexOf("\"id\":${settlement2.id}")
        firstIdIndex shouldBeLessThan secondIdIndex

        verify(exactly = 1) {
            settlementService.readSettlementPage(
                eq(teamId),
                any(SettlementSearchCondition::class),
                any(Pageable::class)
            )
        }
    }

    @Test
    @DisplayName("정산 수정 API 테스트")
    fun updateSettlement_ShouldUpdateAndReturnSettlement() {
        // given
        val settlementId = 1L
        val request = SettlementUpdateRequest(
            amount = BigDecimal.valueOf(2000),
            payerId = 20L,
            settlerId = 10L,
            expenseId = 30L,
            settlementId = settlementId,
            isSettled = false,
        )

        val mockResponse = genSettlement(
            id = settlementId,
            amount = request.amount!!.toLong(),
            payerId = request.payerId!!,
            settlerId = request.settlerId!!,
            expenseId = request.expenseId!!,
            isSettled = request.isSettled!!,
        )

        every {
            settlementService.updateSettlement(
                eq(settlementId),
                any(SettlementUpdateRequest::class)
            )
        } returns mockResponse

        // when
        val result = mockMvc.perform(
            patch("/api/settlements/${settlementId}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andReturn()

        // then
        val content = result.response.contentAsString
        val response = objectMapper.readValue(content, SettlementResponse::class.java)

        response.id shouldBe settlementId
        response.amount shouldBe request.amount
        response.settlerId shouldBe request.settlerId
        response.payerId shouldBe request.payerId
        response.expenseId shouldBe request.expenseId
        response.isSettled shouldBe request.isSettled

        verify(exactly = 1) {
            settlementService.updateSettlement(
                eq(settlementId),
                any(SettlementUpdateRequest::class)
            )
        }
    }

    @Test
    @DisplayName("정산 완료 처리 API 테스트")
    fun settleSettlement_ShouldMarkAsSettled() {
        // given
        val settlementId = 1L
        val request = SettlementUpdateRequest(
            amount = BigDecimal.valueOf(1000),
            payerId = 20L,
            settlerId = 10L,
            expenseId = 30L,
            settlementId = settlementId,
            isSettled = false,
        )

        val mockResponse = genSettlement(id = settlementId, isSettled = true)

        every { settlementService.settleSettlement(settlementId) } returns mockResponse

        // when
        val result = mockMvc.perform(
            patch("/api/settlements/${settlementId}")
                .param("settledOnly", "true")  // settledOnly=true 파라미터 추가
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andReturn()

        // then
        val content = result.response.contentAsString
        val response = objectMapper.readValue(content, SettlementResponse::class.java)

        response.id shouldBe settlementId
        response.isSettled shouldBe true // 정산 완료됨 확인

        verify(exactly = 1) { settlementService.settleSettlement(settlementId) }
        verify(exactly = 0) {
            settlementService.updateSettlement(any(), any())
        } // updateSettlement는 호출되지 않음
    }
}

