package com.luckyseven.backend.domain.team.service

import com.luckyseven.backend.domain.budget.dao.BudgetRepository
import com.luckyseven.backend.domain.budget.entity.Budget
import com.luckyseven.backend.domain.budget.entity.CurrencyCode
import com.luckyseven.backend.domain.expense.entity.Expense
import com.luckyseven.backend.domain.expense.enums.ExpenseCategory
import com.luckyseven.backend.domain.expense.enums.PaymentMethod
import com.luckyseven.backend.domain.expense.repository.CategoryExpenseSum
import com.luckyseven.backend.domain.expense.repository.ExpenseRepository
import com.luckyseven.backend.domain.member.entity.Member
import com.luckyseven.backend.domain.member.repository.MemberRepository
import com.luckyseven.backend.domain.member.service.utill.MemberDetails
import com.luckyseven.backend.domain.settlement.dao.SettlementRepository
import com.luckyseven.backend.domain.settlement.entity.Settlement
import com.luckyseven.backend.domain.team.cache.TeamDashboardCacheService
import com.luckyseven.backend.domain.team.dto.TeamCreateRequest
import com.luckyseven.backend.domain.team.dto.TeamDashboardResponse
import com.luckyseven.backend.domain.team.entity.Team
import com.luckyseven.backend.domain.team.entity.TeamMember
import com.luckyseven.backend.domain.team.enums.TeamStatus
import com.luckyseven.backend.domain.team.repository.TeamMemberRepository
import com.luckyseven.backend.domain.team.repository.TeamRepository
import com.luckyseven.backend.sharedkernel.exception.CustomLogicException
import com.luckyseven.backend.sharedkernel.exception.ExceptionCode
import io.kotest.assertions.assertSoftly
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*
import kotlin.math.exp

class TeamServiceTest : FunSpec({
    val teamRepository = mockk<TeamRepository>()
    val teamMemberRepository = mockk<TeamMemberRepository>()
    val memberRepository = mockk<MemberRepository>()
    val budgetRepository = mockk<BudgetRepository>()
    val settlementRepository = mockk<SettlementRepository>()
    val expenseRepository = mockk<ExpenseRepository>()
    val passwordEncoder = mockk<BCryptPasswordEncoder>()
    val teamDashboardCacheService = mockk<TeamDashboardCacheService>()

    //테스트 대상 서비스
    val teamService = object : TeamService(
        teamRepository,
        teamMemberRepository,
        settlementRepository,
        memberRepository,
        budgetRepository,
        expenseRepository,
        passwordEncoder,
        teamDashboardCacheService
    ) {}
    lateinit var creator: Member

    lateinit var memberDetails: MemberDetails
    lateinit var request: TeamCreateRequest
    lateinit var team: Team
    lateinit var teamMember: TeamMember

    beforeTest {
        creator = Member(
            id = 1L,
            email = "test@example.com",
            nickname = "테스터",
            password = "password123" // This was missing in the original code
        )

        memberDetails = MemberDetails(creator)

        request = TeamCreateRequest(
            name = "test_team",
            teamPassword = "password123"
        )

        team = Team(
            id = 1L,
            name = "test_team",
            teamCode = "ABCDEF",
            leader = creator,
            teamPassword = "password123"
        )

        teamMember = TeamMember(
            id = 1L,
            member = creator,
            team = team,
        )

    }
    // CategoryExpenseSum 이 Interface라 구현 클래스 만들어 사용
    data class CategoryExpenseSumImpl(
        override val category: ExpenseCategory,
        override val totalAmount: BigDecimal
    ) : CategoryExpenseSum

    test("주어진 조건으로 CreateTeam을 호출하면 새로운 team이 create가 되어야 한다") {
        every { memberRepository.findById(creator.id!!) } returns Optional.of(creator)
        every { teamRepository.save(any()) } returns team
        every { teamMemberRepository.save(any()) } returns teamMember

        //when
        val result = teamService.createTeam(memberDetails, request)

        //Then
        // 여러개 assert 시에는 assertSoftly가 좋다네요
        assertSoftly {
            result.shouldNotBeNull()
            result.id shouldBe team.id
            result.name shouldBe request.name
            result.leaderId shouldBe creator.id
        }

        verify { teamRepository.save(any()) }
    }

    test("joinTeam은 존재하는 team에 join 할 때 멤버를 허가해야 한다.") {

        // Given
        val teamCode = "ABCDEF"
        val teamPassword = "password123"
        val newMember = Member(
            id = 2L,
            email = "newmember@example.com",
            nickname = "newMem",
            password = "password123"
        )
        val newMemberDetails = MemberDetails(newMember)
        val teamMember = TeamMember(
            id = 2L,
            member = newMember,
            team = team
        )

        // Mock repository calls
        every { memberRepository.findById(newMember.id!!) } returns Optional.of(newMember)
        every { teamRepository.findByTeamCode(teamCode) } returns team
        every { teamMemberRepository.existsByTeamAndMember(team, newMember) } returns false
        every { teamMemberRepository.save(any()) } returns teamMember

        // When
        val result = teamService.joinTeam(newMemberDetails, teamCode, teamPassword)

        // Then
        assertSoftly {
            result.shouldNotBeNull()
            result.id shouldBe team.id
            result.teamName shouldBe team.name
            result.teamCode shouldBe teamCode
            result.leaderId shouldBe creator.id
        }

        verify { teamMemberRepository.save(any()) }
    }

    test("joinTeam은 이미 가입된 멤버가 join 시도할 때 예외를 발생시켜야 한다.") {
        val teamCode = "ABCDEF"
        val teamPassword = "password123"
        val existingMember = Member(
            id = 2L,
            email = "existing@example.com",
            nickname = "existingMem",
            password = "password123"
        )
        val existingMemberDetails = MemberDetails(existingMember)

        // Mock repository calls
        every { memberRepository.findById(existingMember.id!!) } returns Optional.of(existingMember)
        every { teamRepository.findByTeamCode(teamCode) } returns team
        //  existingMember로 확인하면 true로 리턴 => 이미 존재 한다고 모킹
        every { teamMemberRepository.existsByTeamAndMember(team, existingMember) } returns true

        // When/Then
        val exception = shouldThrow<CustomLogicException> {
            teamService.joinTeam(existingMemberDetails, teamCode, teamPassword)
        }

        exception.exceptionCode shouldBe ExceptionCode.ALREADY_TEAM_MEMBER
    }

    test("joinTeam은 잘못된 비밀번호로 join 시도할 때 예외를 발생시켜야 한다.") {
        val teamCode = "ABCDEF"
        val wrongPwd = "wrongPwd"
        val newMember = Member(
            id = 2L,
            email = "new@example.com",
            nickname = "newMem",
            password = "password123"
        )
        val newMemberDetails = MemberDetails(newMember)

        // repository 모킹
        every { memberRepository.findById(newMember.id!!) } returns Optional.of(newMember)
        every { teamRepository.findByTeamCode(teamCode) } returns team

        // When/Then
        val exception = shouldThrow<CustomLogicException> {
            teamService.joinTeam(newMemberDetails, teamCode, wrongPwd)
        }
        exception.message shouldBe CustomLogicException(ExceptionCode.TEAM_PASSWORD_MISMATCH).message
    }

    test("getTeamsByMemberId는 멤버가 속한 팀 목록을 반환해야 한다.") {
        val memberId = 1L
        val team2 = Team(
            id = 2L,
            name = "second_team",
            teamCode = "GHIJKL",
            leader = creator,
            teamPassword = "password456"
        )

        val teamMember1 = TeamMember(
            id = 1L,
            member = creator,
            team = team
        )

        val teamMember2 = TeamMember(
            id = 2L,
            member = creator,
            team = team2
        )

        val teamMembers = listOf(teamMember1, teamMember2)
        // repository 모킹
        every { teamMemberRepository.findByMemberId(memberId) } returns teamMembers

        //When
        val result = teamService.getTeamsByMemberId(memberId)

        //Then
        assertSoftly {
            result.shouldNotBeNull()
            result.size shouldBe 2
            result[0].id shouldBe team.id
            result[0].name shouldBe team.name
            result[0].teamCode shouldBe team.teamCode
            result[1].id shouldBe team2.id
            result[1].name shouldBe team2.name
            result[1].teamCode shouldBe team2.teamCode
        }
        verify { teamMemberRepository.findByMemberId(memberId) }
    }

    test("getTeamsByMemberId는 존재하지 않는 멤버 ID로 조회 시 예외를 발생시켜야 한다.") {
        val nonExistentMemberId = 999L

        // repository 모킹
        every { memberRepository.findById(nonExistentMemberId) } returns Optional.empty()

        // When/Then
        val exception = shouldThrow<CustomLogicException> {
            teamService.getTeamsByMemberId(nonExistentMemberId)
        }

        exception.exceptionCode shouldBe ExceptionCode.MEMBER_ID_NOTFOUND
    }

    test("getTeamDashboard는 캐시된 데이터가 있고 Budget의 updatedAt이 일치하면 캐시에서 반환해야 한다.") {
        val teamId = 1L
        val now = LocalDateTime.now()

        // Create 캐시된 dashboard
        val cachedDashboard = TeamDashboardResponse(
            teamId = teamId,
            teamName = "test_team",
            teamCode = "ABCDEF",
            teamPassword = "password123",
            updatedAt = now
        )

        // repository 모킹
        every { teamDashboardCacheService.getCachedTeamDashboard(teamId) } returns cachedDashboard
        every { budgetRepository.findUpdatedAtByTeamId(teamId) } returns now

        // When
        val result = teamService.getTeamDashboard(teamId)

        // Then
        assertSoftly {
            result.shouldNotBeNull()
            result.teamId shouldBe teamId
            result.teamName shouldBe "test_team"
            result.updatedAt shouldBe now
        }

        // Verify that refreshTeamDashboard was not called
        verify(exactly = 0) { teamRepository.findById(any()) }
    }

    test("getTeamDashboard는 캐시된 데이터가 없으면 refreshTeamDashboard를 호출해야 한다.") {
        val teamId = 1L
        val now = LocalDateTime.now()

        // Create budget, expenses, category sums. refreshTeamDashboard를 위한 생성
        val budget = Budget(
            id = 1L,
            team = team,
            balance = BigDecimal("1000.00"),
            foreignCurrency = CurrencyCode.USD,
            foreignBalance = BigDecimal("100.00"),
            totalAmount = BigDecimal("1100.00"),
            avgExchangeRate = BigDecimal("10.00"),
            setBy = creator.id ?: 1L
        )
        budget.updatedAt = now

        val expense = Expense(
            id = 1L,
            team = team,
            payer = creator,
            amount = BigDecimal("100.00"),
            description = "Test expense",
            category = ExpenseCategory.MEAL,
            paymentMethod = PaymentMethod.CASH,
        )
        expense.createdAt = now

        val categorySums = listOf(
            CategoryExpenseSumImpl(ExpenseCategory.MEAL, BigDecimal("100.00"))
        )

        // repository 모킹
        every { teamDashboardCacheService.getCachedTeamDashboard(teamId) } returns null
        every { teamRepository.findById(teamId) } returns Optional.of(team)
        every { budgetRepository.findByTeamId(teamId) } returns budget

        val pageableSlot = slot<Pageable>()
        every { expenseRepository.findByTeamId(eq(teamId), capture(pageableSlot)) } returns
                PageImpl(listOf(expense))

        every { expenseRepository.findCategoryExpenseSumsByTeamId(teamId) } returns categorySums
        every { teamDashboardCacheService.cacheTeamDashboard(eq(teamId), any()) } returns Unit

        // When
        val result = teamService.getTeamDashboard(teamId)

        // Then
        assertSoftly {
            result.shouldNotBeNull()
            result.teamId shouldBe teamId
            result.teamName shouldBe "test_team"
            result.teamCode shouldBe "ABCDEF"
            result.foreignCurrency shouldBe CurrencyCode.USD
            result.balance shouldBe BigDecimal("1000.00")
            result.expenseList.size shouldBe 1
            result.categoryExpenseSumList.size shouldBe 1
        }

        // refreshDashboard가 호출되었는지 검증.
        verify { teamRepository.findById(teamId) }
        verify { budgetRepository.findByTeamId(teamId) }
        verify { expenseRepository.findByTeamId(eq(teamId), any()) }
        verify { expenseRepository.findCategoryExpenseSumsByTeamId(teamId) }
        verify { teamDashboardCacheService.cacheTeamDashboard(eq(teamId), any()) }
    }

    test("refreshTeamDashboard는 팀 ID로 대시보드 데이터를 새로 조회하고 캐시에 저장해야 한다.") {
        val teamId = 1L
        val now = LocalDateTime.now()

        // Create budget, expenses, category sums 모킹
        val budget = Budget(
            id = 1L,
            team = team,
            balance = BigDecimal("1000.00"),
            foreignCurrency = CurrencyCode.USD,
            foreignBalance = BigDecimal("100.00"),
            totalAmount = BigDecimal("1100.00"),
            avgExchangeRate = BigDecimal("10.00"),
            setBy = creator.id ?: 1L
        )
        budget.updatedAt = now
        val expense = Expense(
            id = 1L,
            team = team,
            payer = creator,
            amount = BigDecimal("100.00"),
            description = "Test expense",
            category = ExpenseCategory.MEAL,
            paymentMethod = PaymentMethod.CASH,
        )
        expense.createdAt = now


        val categorySums = listOf(
            CategoryExpenseSumImpl(ExpenseCategory.MEAL, BigDecimal("100.00"))
        )

        every { teamRepository.findById(teamId) } returns Optional.of(team)
        every { budgetRepository.findByTeamId(teamId) } returns budget

        val pageableSlot = slot<Pageable>()
        every { expenseRepository.findByTeamId(eq(teamId), capture(pageableSlot)) } returns
                PageImpl(listOf(expense))

        every { expenseRepository.findCategoryExpenseSumsByTeamId(teamId) } returns categorySums
        every { teamDashboardCacheService.cacheTeamDashboard(eq(teamId), any()) } returns Unit

        // When
        val result = teamService.refreshTeamDashboard(teamId)

        // Then
        assertSoftly {
            result.shouldNotBeNull()
            result.teamId shouldBe teamId
            result.teamName shouldBe "test_team"
            result.teamCode shouldBe "ABCDEF"
            result.foreignCurrency shouldBe CurrencyCode.USD
            result.balance shouldBe BigDecimal("1000.00")
            result.expenseList.size shouldBe 1
            result.categoryExpenseSumList.size shouldBe 1
        }

        // 검증
        verify { teamRepository.findById(teamId) }
        verify { budgetRepository.findByTeamId(teamId) }
        verify { expenseRepository.findByTeamId(eq(teamId), any()) }
        verify { expenseRepository.findCategoryExpenseSumsByTeamId(teamId) }
        verify { teamDashboardCacheService.cacheTeamDashboard(eq(teamId), any()) }
    }

    test("refreshTeamDashboard는 존재하지 않는 팀 ID로 조회 시 예외를 발생시켜야 한다.") {
        val nonExistentTeamId = 999L

        every { teamRepository.findById(nonExistentTeamId) } returns Optional.empty()

        // When/Then
        val exception = shouldThrow<CustomLogicException> {
            teamService.refreshTeamDashboard(nonExistentTeamId)
        }

        exception.exceptionCode shouldBe ExceptionCode.TEAM_NOT_FOUND
    }

    test("deleteMarkedTeams는 삭제 예정으로 표시된 팀과 관련 데이터를 모두 삭제해야 한다.") {
        // Given
        val now = LocalDateTime.now()
        val teamToDelete = Team(
            id = 3L,
            name = "team_to_delete",
            teamCode = "DELETE1",
            teamPassword = "password123",
            leader = creator,
            status = TeamStatus.MARKED_FOR_DELETE,
            deletionScheduledAt = now.minusDays(1) // Past date to trigger deletion
        )

        // 연관 데이터 생성
        val teamMember1 = TeamMember(
            id = 10L,
            member = creator,
            team = teamToDelete
        )

        val budget = Budget(
            id = 3L,
            team = teamToDelete,
            balance = BigDecimal("1000.00"),
            foreignCurrency = CurrencyCode.USD,
            foreignBalance = BigDecimal("100.00"),
            totalAmount = BigDecimal("1100.00"),
            avgExchangeRate = BigDecimal("10.00"),
            setBy = creator.id ?: 1L
        )

        val expense = Expense(
            id = 5L,
            team = teamToDelete,
            payer = creator,
            amount = BigDecimal("100.00"),
            description = "Test expense",
            category = ExpenseCategory.MEAL,
            paymentMethod = PaymentMethod.CASH
        )

        val settlement = Settlement(
            id = 7L,
            amount = BigDecimal("50.00"),
            isSettled = false,
            settler = creator,
            payer = creator,
            expense = expense
        )

        every { teamRepository.findByStatusAndDeletionScheduledAt(TeamStatus.MARKED_FOR_DELETE, any()) } returns listOf(teamToDelete)

        val pageableSlot = slot<Pageable>()
        every { expenseRepository.findByTeamId(eq(teamToDelete.id!!), capture(pageableSlot)) } returns
                PageImpl(listOf(expense))

        every { teamMemberRepository.findByTeamId(teamToDelete.id!!) } returns listOf(teamMember1)
        every { budgetRepository.findByTeamId(teamToDelete.id!!) } returns budget
        every { settlementRepository.findByExpenseIdIn(listOf(expense.id!!)) } returns listOf(settlement)

        // delete 목업
        every { settlementRepository.deleteAll(any<List<Settlement>>()) } returns Unit
        every { expenseRepository.deleteAll(any<List<Expense>>()) } returns Unit
        every { teamMemberRepository.deleteAll(any<List<TeamMember>>()) } returns Unit
        every { budgetRepository.delete(any()) } returns Unit
        every { teamRepository.delete(any()) } returns Unit

        // When
        teamService.deleteMarkedTeams()

        // Then
        verify { teamRepository.findByStatusAndDeletionScheduledAt(TeamStatus.MARKED_FOR_DELETE, any()) }
        verify { expenseRepository.findByTeamId(teamToDelete.id!!, any()) }
        verify { teamMemberRepository.findByTeamId(teamToDelete.id!!) }
        verify { budgetRepository.findByTeamId(teamToDelete.id!!) }

        verify { settlementRepository.findByExpenseIdIn(listOf(expense.id!!))}
        verify { settlementRepository.deleteAll(match { it.contains(settlement) })}
        verify { expenseRepository.deleteAll(match { it.contains(expense) }) }
        verify { teamMemberRepository.deleteAll(match { it.contains(teamMember1) }) }
        verify { budgetRepository.delete(budget) }
        verify { teamRepository.delete(teamToDelete) }
    }

})
