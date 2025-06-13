package com.luckyseven.backend.domain.expense.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.luckyseven.backend.domain.expense.dto.*
import com.luckyseven.backend.domain.expense.enums.ExpenseCategory
import com.luckyseven.backend.domain.expense.enums.PaymentMethod
import com.luckyseven.backend.domain.expense.service.ExpenseService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import java.math.BigDecimal
import java.time.LocalDateTime

class ExpenseControllerTest {

    private lateinit var mockMvc: MockMvc

    @Mock
    private lateinit var expenseService: ExpenseService

    @InjectMocks
    private lateinit var expenseController: ExpenseController

    private lateinit var objectMapper: ObjectMapper
    private lateinit var request: ExpenseRequest
    private lateinit var createResponse: CreateExpenseResponse
    private lateinit var expenseResponse: ExpenseResponse
    private lateinit var balanceResponse: ExpenseBalanceResponse

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        mockMvc = MockMvcBuilders.standaloneSetup(expenseController).build()
        objectMapper = ObjectMapper()

        request = ExpenseRequest(
            description = "Test expense",
            amount = BigDecimal("1000.00"),
            category = ExpenseCategory.MEAL,
            payerId = 1L,
            paymentMethod = PaymentMethod.CASH,
            settlerId = mutableListOf(1L)
        )
        createResponse = CreateExpenseResponse(
            id = 1L,
            amount = BigDecimal("1000.00"),
            foreignBalance = BigDecimal("8000.00"),
            balance = BigDecimal("9000.00"),
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        expenseResponse = ExpenseResponse(
            id = 1L,
            description = "Test expense",
            amount = BigDecimal("1000.00"),
            category = ExpenseCategory.MEAL,
            paymentMethod = PaymentMethod.CASH,
            payerId = 1L,
            payerNickname = "TestUser",
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        balanceResponse = ExpenseBalanceResponse(
            foreignBalance = BigDecimal("8000.00"),
            balance = BigDecimal("9000.00")
        )
    }

    @Test
    @DisplayName("POST /api/{teamId}/expense - Success")
    fun `create expense returns 201`() {
        whenever(expenseService.saveExpense(eq(1L), any<ExpenseRequest>()))
            .thenReturn(createResponse)

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

    @Test
    @DisplayName("GET /api/expense/{expenseId} - Success")
    fun `get expense returns 200`() {
        whenever(expenseService.getExpense(eq(1L)))
            .thenReturn(expenseResponse)

        mockMvc.perform(get("/api/expense/1"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.description").value("Test expense"))
            .andExpect(jsonPath("$.amount").value(1000.00))
            .andExpect(jsonPath("$.payerNickname").value("TestUser"))
    }

    @Test
    @DisplayName("PATCH /api/expense/{expenseId} - Success")
    fun `update expense returns 200`() {
        val updateRequest = ExpenseUpdateRequest(
            description = "Updated",
            amount = BigDecimal("2000.00"),
            category = ExpenseCategory.MEAL
        )
        whenever(expenseService.updateExpense(eq(1L), any<ExpenseUpdateRequest>()))
            .thenReturn(createResponse)

        mockMvc.perform(
            patch("/api/expense/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(1))
    }

    @Test
    @DisplayName("DELETE /api/expense/{expenseId} - Success")
    fun `delete expense returns 200`() {
        whenever(expenseService.deleteExpense(eq(1L)))
            .thenReturn(balanceResponse)

        mockMvc.perform(delete("/api/expense/1"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.balance").value(9000.00))
            .andExpect(jsonPath("$.foreignBalance").value(8000.00))
    }
}
