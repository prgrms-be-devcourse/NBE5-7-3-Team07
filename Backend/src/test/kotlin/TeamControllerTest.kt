import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver
import com.fasterxml.jackson.databind.ObjectMapper
import com.luckyseven.backend.domain.budget.entity.CurrencyCode
import com.luckyseven.backend.domain.expense.enums.ExpenseCategory
import com.luckyseven.backend.domain.expense.enums.PaymentMethod
import com.luckyseven.backend.domain.expense.repository.CategoryExpenseSum
import com.luckyseven.backend.domain.member.entity.Member
import com.luckyseven.backend.domain.member.service.utill.MemberDetails
import com.luckyseven.backend.domain.team.controller.TeamController
import com.luckyseven.backend.domain.team.dto.*
import com.luckyseven.backend.domain.team.entity.Team
import com.luckyseven.backend.domain.team.entity.TeamMember
import com.luckyseven.backend.domain.team.service.TeamMemberService
import com.luckyseven.backend.domain.team.service.TeamService
import com.luckyseven.backend.sharedkernel.exception.CustomExceptionHandler
import com.luckyseven.backend.sharedkernel.exception.CustomLogicException
import com.luckyseven.backend.sharedkernel.exception.ExceptionCode
import io.kotest.core.spec.style.FunSpec
import io.mockk.*
import org.springframework.http.MediaType
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import java.math.BigDecimal
import java.time.LocalDateTime

class TeamControllerTest : FunSpec({

    // 의존성 모킹
    val teamService = mockk<TeamService>(relaxed = true)
    val teamMemberService = mockk<TeamMemberService>(relaxed = true)
    val objectMapper = ObjectMapper()

    // 컨트롤러 생성
    val teamController = TeamController(teamService, teamMemberService)

    // MockMvc 설정
    val mockMvc = MockMvcBuilders.standaloneSetup(teamController)
        .setControllerAdvice(CustomExceptionHandler())
        .setCustomArgumentResolvers(AuthenticationPrincipalArgumentResolver())
        .build()

    // 테스트에 사용할 객체들
    lateinit var testMember: Member
    lateinit var memberDetails: MemberDetails
    lateinit var testTeam: Team
    lateinit var teamMember: TeamMember

    beforeTest {
        // 테스트 멤버 생성
        testMember = Member(
            id = 1L,
            email = "test@example.com",
            nickname = "테스터",
            password = "password123"
        )

        // MemberDetails 생성
        memberDetails = MemberDetails(testMember)

        // 인증 설정
        val auth = UsernamePasswordAuthenticationToken(
            memberDetails, null, memberDetails.authorities
        )
        SecurityContextHolder.getContext().authentication = auth

        // 테스트 팀 생성
        testTeam = Team(
            id = 1L,
            name = "테스트팀",
            teamCode = "ABCDEF",
            teamPassword = "password123",
            leader = testMember
        )

        // 팀 멤버 생성
        teamMember = TeamMember(
            id = 1L,
            member = testMember,
            team = testTeam
        )
    }

    afterTest {
        // 인증 정보 초기화
        SecurityContextHolder.clearContext()
        clearAllMocks()
    }

    // 팀 생성 테스트
    test("createTeam은 유효한 요청으로 팀 생성 시 200 OK를 응답해야 한다") {
        // Given
        val request = TeamCreateRequest(
            name = "새로운팀",
            teamPassword = "password123"
        )

        val response = TeamCreateResponse(
            id = 1L,
            name = "새로운팀",
            teamCode = "ABCDEF",
            leaderId = testMember.id
        )

        every { teamService.createTeam(any(), any()) } returns response

        // When & Then
        mockMvc.perform(post("/api/teams")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.name").value("새로운팀"))
            .andExpect(jsonPath("$.teamCode").value("ABCDEF"))
            .andExpect(jsonPath("$.leaderId").value(1))
            .andDo(print())

        verify { teamService.createTeam(any(), any()) }
    }

    // 팀 참가 테스트
    test("joinTeam은 유효한 요청으로 팀 참가 시 200 OK를 응답해야 한다") {
        // Given
        val request = TeamJoinRequest(
            teamCode = "ABCDEF",
            teamPassword = "password123"
        )

        val response = TeamJoinResponse(
            id = 1L,
            teamName = "테스트팀",
            teamCode = "ABCDEF",
            leaderId = testMember.id
        )

        every { teamService.joinTeam(any(), any(), any()) } returns response

        // When & Then
        mockMvc.perform(post("/api/teams/members")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.teamName").value("테스트팀"))
            .andExpect(jsonPath("$.teamCode").value("ABCDEF"))
            .andExpect(jsonPath("$.leaderId").value(1))
            .andDo(print())

        verify { teamService.joinTeam(any(), eq("ABCDEF"), eq("password123")) }
    }

    // 내 팀 목록 조회 테스트
    test("getMyTeams은 로그인한 사용자의 팀 목록을 조회하고 200 OK를 응답해야 한다") {
        // Given
        val teams = listOf(
            TeamListResponse(
                id = 1L,
                name = "테스트팀1",
                teamCode = "ABCDEF"
            ),
            TeamListResponse(
                id = 2L,
                name = "테스트팀2",
                teamCode = "GHIJKL"
            )
        )

        every { teamService.getTeamsByMemberId(testMember.id!!) } returns teams

        // When & Then
        mockMvc.perform(get("/api/teams/myTeams"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$").isArray)
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[0].name").value("테스트팀1"))
            .andExpect(jsonPath("$[1].id").value(2))
            .andExpect(jsonPath("$[1].name").value("테스트팀2"))
            .andDo(print())

        verify { teamService.getTeamsByMemberId(testMember.id!!) }
    }

    // 팀 멤버 조회 테스트
    test("getTeamMembers는 팀 ID로 멤버 목록을 조회하고 200 OK를 응답해야 한다") {
        // Given
        val teamId = 1L
        val teamMembers = listOf(
            TeamMemberDto(
                id = 1L,
                teamId = teamId,
                teamName = "테스트팀",
                memberId = 1L,
                memberNickName = "테스터",
                memberEmail = "test@example.com",
                role = "Leader"
            ),
            TeamMemberDto(
                id = 2L,
                teamId = teamId,
                teamName = "테스트팀",
                memberId = 2L,
                memberNickName = "멤버1",
                memberEmail = "member1@example.com",
                role = "Member"
            )
        )

        every { teamMemberService.getTeamMemberByTeamId(teamId) } returns teamMembers

        // When & Then
        mockMvc.perform(get("/api/teams/$teamId/members"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$").isArray)
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[0].teamName").value("테스트팀"))
            .andExpect(jsonPath("$[0].role").value("Leader"))
            .andExpect(jsonPath("$[1].id").value(2))
            .andExpect(jsonPath("$[1].role").value("Member"))
            .andDo(print())

        verify { teamMemberService.getTeamMemberByTeamId(teamId) }
    }

    // 팀 멤버 삭제 테스트
    test("removeTeamMember는 팀 멤버 삭제 시 204 No Content를 응답해야 한다") {
        // Given
        val teamId = 1L
        val teamMemberId = 2L

        every { teamMemberService.removeTeamMember(any(), teamId, teamMemberId) } just runs

        // When & Then
        mockMvc.perform(delete("/api/teams/$teamId/members/$teamMemberId"))
            .andExpect(status().isNoContent)
            .andDo(print())

        verify { teamMemberService.removeTeamMember(any(), teamId, teamMemberId) }
    }

    // 팀 대시보드 조회 테스트
    test("getTeamDashboard는 팀 대시보드 조회 시 200 OK를 응답해야 한다") {
        // Given
        val teamId = 1L
        val now = LocalDateTime.now()

        // CategoryExpenseSum 인터페이스 구현 클래스
        data class CategoryExpenseSumImpl(
            override val category: ExpenseCategory,
            override val totalAmount: BigDecimal
        ) : CategoryExpenseSum

        val dashboardResponse = TeamDashboardResponse(
            teamId = teamId,
            teamName = "테스트팀",
            teamCode = "ABCDEF",
            teamPassword = "password123",
            foreignCurrency = CurrencyCode.USD,
            balance = BigDecimal("1000.00"),
            foreignBalance = BigDecimal("100.00"),
            totalAmount = BigDecimal("1000.00"),
            avgExchangeRate = BigDecimal("10.00"),
            updatedAt = now,
            expenseList = listOf(
                TeamDashboardResponse.ExpenseDto(
                    id = 1L,
                    description = "점심 식사",
                    amount = BigDecimal("100.00"),
                    category = ExpenseCategory.MEAL,
                    paymentMethod = PaymentMethod.CARD,
                    date = now,
                    payerNickname = "테스터"
                )
            ),
            categoryExpenseSumList = listOf(
                TeamDashboardResponse.CategoryExpenseSumDto(
                    category = ExpenseCategory.MEAL,
                    totalAmount = BigDecimal("100.00")
                )
            )
        )

        every { teamService.getTeamDashboard(teamId) } returns dashboardResponse

        // When & Then
        mockMvc.perform(get("/api/teams/$teamId/dashboard"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.teamId").value(teamId))
            .andExpect(jsonPath("$.teamName").value("테스트팀"))
            .andExpect(jsonPath("$.foreignCurrency").value("USD"))
            .andExpect(jsonPath("$.balance").value(1000.00))
            .andExpect(jsonPath("$.expenseList").isArray)
            .andExpect(jsonPath("$.expenseList.length()").value(1))
            .andExpect(jsonPath("$.expenseList[0].description").value("점심 식사"))
            .andExpect(jsonPath("$.categoryExpenseSumList").isArray)
            .andExpect(jsonPath("$.categoryExpenseSumList.length()").value(1))
            .andExpect(jsonPath("$.categoryExpenseSumList[0].category").value("MEAL"))
            .andDo(print())

        verify { teamService.getTeamDashboard(teamId) }
    }

    // 예외 테스트 - 팀 멤버 조회 실패
    test("getTeamMembers는 존재하지 않는 팀 ID로 조회 시 404 Not Found를 응답해야 한다") {
        // Given
        val nonExistentTeamId = 999L

        every { teamMemberService.getTeamMemberByTeamId(nonExistentTeamId) } throws
                CustomLogicException(ExceptionCode.TEAM_NOT_FOUND)

        // When & Then
        mockMvc.perform(get("/api/teams/$nonExistentTeamId/members"))
            .andExpect(status().isNotFound)
            .andDo(print())

        verify { teamMemberService.getTeamMemberByTeamId(nonExistentTeamId) }
    }

    // 예외 테스트 - 팀 참가 실패 (이미 가입된 멤버)
    test("joinTeam은 이미 가입된 멤버가 참가 시도 시 400 Bad Request를 응답해야 한다") {
        // Given
        val request = TeamJoinRequest(
            teamCode = "ABCDEF",
            teamPassword = "password123"
        )

        every { teamService.joinTeam(any(), any(), any()) } throws
                CustomLogicException(ExceptionCode.ALREADY_TEAM_MEMBER)

        // When & Then
        mockMvc.perform(post("/api/teams/members")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest)
            .andDo(print())

        verify { teamService.joinTeam(any(), any(), any()) }
    }

    // 예외 테스트 - 팀 참가 실패 (비밀번호 불일치)
    test("joinTeam은 잘못된 비밀번호로 참가 시도 시 400 Bad Request를 응답해야 한다") {
        // Given
        val request = TeamJoinRequest(
            teamCode = "ABCDEF",
            teamPassword = "wrongpassword"
        )

        every { teamService.joinTeam(any(), any(), any()) } throws
                CustomLogicException(ExceptionCode.TEAM_PASSWORD_MISMATCH)

        // When & Then
        mockMvc.perform(post("/api/teams/members")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest)
            .andDo(print())

        verify { teamService.joinTeam(any(), any(), any()) }
    }
})
