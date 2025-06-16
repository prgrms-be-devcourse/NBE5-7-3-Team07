package com.luckyseven.backend.domain.expense.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.luckyseven.backend.domain.expense.dto.ExpenseUpdateRequest
import com.luckyseven.backend.domain.expense.enums.ExpenseCategory
import com.luckyseven.backend.domain.expense.service.ExpenseService
import com.luckyseven.backend.domain.expense.util.ExpenseTestUtils
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import java.math.BigDecimal

@ExtendWith(MockKExtension::class)
class ExpenseControllerTest {

    private lateinit var mockMvc: MockMvc
    private val objectMapper = ObjectMapper()

    @MockK
    private lateinit var expenseService: ExpenseService

    @InjectMockKs
    private lateinit var expenseController: ExpenseController

    @BeforeEach
    fun setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(expenseController).build()
    }

    @Nested
    @DisplayName("지출 성공")
    inner class CreateExpenseTests {
        @Test
        @DisplayName("POST /api/{teamId}/expense - Success")
        fun `create expense returns 201`() {
            val request = ExpenseTestUtils.buildRequest()
            val createResponse = ExpenseTestUtils.buildCreateResponse()

            every { expenseService.saveExpense(1L, request) } returns createResponse

            mockMvc.perform(
                post("/api/1/expense")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isCreated)
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.amount").value(1000.00))
                .andExpect(jsonPath("$.balance").value(9000.00))
                .andExpect(jsonPath("$.foreignBalance").value(8000.00))
        }
    }

    @Nested
    @DisplayName("지출 조회")
    inner class GetExpenseTests {
        @Test
        @DisplayName("GET /api/expense/{expenseId} - Success")
        fun `get expense returns 200`() {
            val response = ExpenseTestUtils.buildExpenseResponse()
            every { expenseService.getExpense(1L) } returns response

            mockMvc.perform(get("/api/expense/1"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.description").value("Test expense"))
                .andExpect(jsonPath("$.amount").value(1000.00))
                .andExpect(jsonPath("$.payerNickname").value("TestUser"))
        }
    }

    @Nested
    @DisplayName("지출 수정")
    inner class UpdateExpenseTests {
        @Test
        @DisplayName("PATCH /api/expense/{expenseId} - Success")
        fun `update expense returns 200`() {
            // given
            val updateRequest = ExpenseUpdateRequest(
                description = "Updated",
                amount = BigDecimal("2000.00"),
                category = ExpenseCategory.MEAL
            )
            val createResponse = ExpenseTestUtils.buildCreateResponse(
                id = 1L,
                amount = BigDecimal("2000.00"),
                balance = BigDecimal("8000.00"),
                foreignBalance = BigDecimal("8000.00")
            )
            every { expenseService.updateExpense(1L, updateRequest) } returns createResponse

            mockMvc.perform(
                patch("/api/expense/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateRequest))
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.amount").value(2000.00))
                .andExpect(jsonPath("$.balance").value(8000.00))
                .andExpect(jsonPath("$.foreignBalance").value(8000.00))

            verify(exactly = 1) { expenseService.updateExpense(1L, updateRequest) }
        }
    }

    @Nested
    @DisplayName("지출 삭제")
    inner class DeleteExpenseTests {
        @Test
        @DisplayName("DELETE /api/expense/{expenseId} - Success")
        fun `delete expense returns 200`() {
            val balanceResponse = ExpenseTestUtils.buildBalanceResponse()
            every { expenseService.deleteExpense(1L) } returns balanceResponse

            mockMvc.perform(delete("/api/expense/1"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.balance").value(9000.00))
                .andExpect(jsonPath("$.foreignBalance").value(8000.00))
        }
    }
}
