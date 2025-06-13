package com.luckyseven.backend.domain.budget.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.luckyseven.backend.domain.budget.dto.*
import com.luckyseven.backend.domain.budget.entity.CurrencyCode
import com.luckyseven.backend.domain.budget.service.BudgetService
import com.luckyseven.backend.domain.budget.util.TestUtils
import com.luckyseven.backend.domain.budget.validator.BudgetValidator
import com.luckyseven.backend.sharedkernel.exception.CustomLogicException
import com.luckyseven.backend.sharedkernel.exception.ExceptionCode
import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.patch
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import java.math.BigDecimal

@WebMvcTest(BudgetController::class)
@AutoConfigureMockMvc(addFilters = false)
class BudgetControllerTests {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockitoBean
    private lateinit var budgetService: BudgetService

    @MockitoBean
    private lateinit var budgetValidator: BudgetValidator

    // SecurityContext 정리
    @AfterEach
    fun tearDown() {
        SecurityContextHolder.clearContext()
    }

    @Test
    fun `POST 요청으로 예산 생성이 성공하면 201 CREATED를 응답한다`() {
        val member = TestUtils.genMemberDetails()
        TestUtils.genAuthentication(member)

        val teamId = 1L
        val req = TestUtils.genBudgetCreateReq()
        val resp = TestUtils.genBudgetCreateResp()

        every { budgetService.save(teamId, member.id, req) } returns resp

        mockMvc.post("/api/teams/${teamId}/budgets") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(req)
        }.andExpect {
            status { isCreated() }
        }.andDo { print() }

        verify(exactly = 1) { budgetValidator.validateRequest(req) }
    }

    @Test
    fun `totalAmount가 누락된 요청을 받으면 400 BAD REQUEST와 함께 예산 생성에 실패한다`() {
        val member = TestUtils.genMemberDetails()
        TestUtils.genAuthentication(member)

        val teamId = 1L

        val req = """
        {
            "isExchanged": true,
            "exchangeRage": 1393.7,
            "foreignCurrency": "USD"
        }
        """.trimIndent()

        mockMvc.post("/api/teams/${teamId}/budgets") {
            contentType = MediaType.APPLICATION_JSON
            content = req
        }.andExpect {
            status { isBadRequest() }
        }.andDo {
            print()
        }
    }

    @Test
    fun `GET 요청으로 팀 예산 조회에 성공하면 200 OK로 응답한다`() {
        val teamId = 1L
        val resp = TestUtils.genBudgetReadResp()

        every { budgetService.getByTeamId(teamId) } returns resp

        mockMvc.get("/api/teams/${teamId}/budgets")
            .andExpect {
                status { isOk() }
            }.andDo {
                print()
            }
    }

    @Test
    fun `존재하지 않는 teamId로 예산 조회를 할 경우 404 NOT FOUND가 발생한다`() {
        val teamId = 1L

        every { budgetService.getByTeamId(teamId) } throws CustomLogicException(ExceptionCode.TEAM_NOT_FOUND)

        mockMvc.get("/api/teams/${teamId}/budgets")
            .andExpect {
                status { isNotFound() }
            }.andDo {
                print()
            }
    }

    @Test
    fun `DELETE 요청으로 예산 삭제에 성공하면 204 NO CONTENT를 응답한다`() {
        val teamId = 1L

        mockMvc.delete("/api/teams/${teamId}/budgets")
            .andExpect {
                status { isNoContent() }
            }.andDo {
                print()
            }

        verify(exactly = 1) { budgetService.deleteByTeamId(teamId) }
    }

    @Test
    fun `PATCH 요청으로 예산 수정에 성공하면 200 OK를 응답한다`() {

        val teamId = 1L
        val member = TestUtils.genMemberDetails()
        TestUtils.genAuthentication(member)

        val req = TestUtils.genBudgetUpdateReq()
        val resp = TestUtils.genBudgetUpdateResp()

        every { budgetService.updateByTeamId(teamId, member.id, req) } returns resp

        mockMvc.patch("/api/teams/${teamId}/budgets") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(req)
        }.andExpect {
            status { isOk() }
        }.andDo {
            print()
        }

        verify(exactly = 1) { budgetValidator.validateRequest(req)}
    }

    @Test
    fun `totalAmount가 누락된 요청이 들어온 경우 예산 수정에 실패하고 400 BAD REQUEST를 응답한다`() {
        val teamId = 1L

        val member = TestUtils.genMemberDetails()
        TestUtils.genAuthentication(member)

        val req = """
        {
            "isExchanged": true,
            "exchangeRate": 1393.7
        }
        """.trimIndent()

        mockMvc.patch("/api/teams/${teamId}/budgets") {
            contentType = MediaType.APPLICATION_JSON
            content = req
        }.andExpect {
            status { isBadRequest() }
        }.andDo {
            print()
        }
    }

    @Test
    fun `PATCH 요청으로 예산 추가에 성공하면 200 OK를 응답한다`() {
        val teamId = 1L

        val member = TestUtils.genMemberDetails()
        TestUtils.genAuthentication(member)

        val req = TestUtils.genBudgetAddReq()
        val resp = TestUtils.genBudgetUpdateRespAfterAdd()

        every { budgetService.addBudgetByTeamId(teamId, member.id, req) } returns resp

        mockMvc.patch("/api/teams/${teamId}/budgets/add") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(req)
        }.andExpect {
            status { isOk() }
        }.andDo {
            print()
        }

        verify(exactly = 1) { budgetValidator.validateRequest(req) }

    }

    @Test
    fun `additionalBudget가 누락된 요청이 들어온 경우 예산 추가에 실패하고 400 BAD REQUEST를 응답한다`() {
        val teamId = 1L

        val req = """
        {
            "isExchanged": false
        }
        """.trimIndent()

        mockMvc.patch("/api/teams/${teamId}/budgets/add") {
            contentType = MediaType.APPLICATION_JSON
            content = req
        }.andExpect {
            status { isBadRequest() }
        }.andDo {
            print()
        }
    }
}