package domain.team.service

import com.luckyseven.backend.domain.member.entity.Member
import com.luckyseven.backend.domain.member.repository.MemberRepository
import com.luckyseven.backend.domain.member.service.utill.MemberDetails
import com.luckyseven.backend.domain.team.entity.Team
import com.luckyseven.backend.domain.team.entity.TeamMember
import com.luckyseven.backend.domain.team.repository.TeamMemberRepository
import com.luckyseven.backend.domain.team.repository.TeamRepository
import com.luckyseven.backend.domain.team.service.TeamMemberService
import com.luckyseven.backend.sharedkernel.exception.CustomLogicException
import com.luckyseven.backend.sharedkernel.exception.ExceptionCode
import io.kotest.assertions.assertSoftly
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.util.*

class TeamMemberServiceTest : FunSpec({
    // 의존성 모킹
    val teamRepository = mockk<TeamRepository>()
    val teamMemberRepository = mockk<TeamMemberRepository>()
    val memberRepository = mockk<MemberRepository>()

    // 테스트 대상 서비스
    val teamMemberService = TeamMemberService(
        teamRepository,
        teamMemberRepository,
        memberRepository
    )

    // 테스트에 사용할 객체들
    lateinit var leader: Member
    lateinit var member: Member
    lateinit var team: Team
    lateinit var teamMember: TeamMember
    lateinit var leaderTeamMember: TeamMember
    lateinit var leaderDetails: MemberDetails

    beforeTest {
        // 팀 리더 생성
        leader = Member(
            id = 1L,
            email = "leader@example.com",
            nickname = "리더",
            password = "password123"
        )

        // 일반 멤버 생성
        member = Member(
            id = 2L,
            email = "member@example.com",
            nickname = "멤버",
            password = "password123"
        )

        // 팀 생성
        team = Team(
            id = 1L,
            name = "테스트 팀",
            teamCode = "ABCDEF",
            leader = leader,
            teamPassword = "password123"
        )

        // 팀 멤버 생성 (일반 멤버)
        teamMember = TeamMember(
            id = 2L,
            member = member,
            team = team
        )

        // 팀 멤버 생성 (리더)
        leaderTeamMember = TeamMember(
            id = 1L,
            member = leader,
            team = team
        )

        // 리더의 MemberDetails 생성
        leaderDetails = MemberDetails(leader)

        // 팀에 멤버 추가
        team.addTeamMember(teamMember)
        team.addTeamMember(leaderTeamMember)
    }

    // getTeamMemberByTeamId 테스트
    test("getTeamMemberByTeamId는 존재하는 팀 ID로 조회 시 팀 멤버 목록을 반환해야 한다") {
        // Given
        val teamId = 1L
        val teamMembers = listOf(leaderTeamMember, teamMember)

        // Mock repository calls
        every { teamRepository.existsById(teamId) } returns true
        every { teamMemberRepository.findByTeamId(teamId) } returns teamMembers

        // When
        val result = teamMemberService.getTeamMemberByTeamId(teamId)

        // Then
        assertSoftly {
            result.size shouldBe 2

            // 첫 번째 멤버 (리더) 검증
            result[0].memberId shouldBe leader.id
            result[0].memberNickName shouldBe leader.nickname
            result[0].memberEmail shouldBe leader.email
            result[0].teamId shouldBe team.id
            result[0].teamName shouldBe team.name
            result[0].role shouldBe "Leader"

            // 두 번째 멤버 검증
            result[1].memberId shouldBe member.id
            result[1].memberNickName shouldBe member.nickname
            result[1].memberEmail shouldBe member.email
            result[1].teamId shouldBe team.id
            result[1].teamName shouldBe team.name
            result[1].role shouldBe "Member"
        }

        verify { teamRepository.existsById(teamId) }
        verify { teamMemberRepository.findByTeamId(teamId) }
    }

    test("getTeamMemberByTeamId는 존재하지 않는 팀 ID로 조회 시 예외를 발생시켜야 한다") {
        // Given
        val nonExistentTeamId = 999L

        // Mock repository calls
        every { teamRepository.existsById(nonExistentTeamId) } returns false

        // When/Then
        val exception = shouldThrow<CustomLogicException> {
            teamMemberService.getTeamMemberByTeamId(nonExistentTeamId)
        }

        exception.exceptionCode shouldBe ExceptionCode.TEAM_NOT_FOUND
    }

    // removeTeamMember 테스트
    test("removeTeamMember는 팀 리더가 일반 멤버를 삭제할 때 성공해야 한다") {
        // Given
        val teamId = 1L
        val teamMemberId = 2L

        every { memberRepository.findById(leader.id!!) } returns Optional.of(leader)
        every { teamRepository.findById(teamId) } returns Optional.of(team)
        every { teamMemberRepository.findById(teamMemberId) } returns Optional.of(teamMember)
        every { teamMemberRepository.deleteById(teamMemberId) } returns Unit

        // When
        teamMemberService.removeTeamMember(leaderDetails, teamId, teamMemberId)

        // Then
        verify { memberRepository.findById(leader.id!!) }
        verify { teamRepository.findById(teamId) }
        verify { teamMemberRepository.findById(teamMemberId) }
        verify { teamMemberRepository.deleteById(teamMemberId) }
    }

    test("removeTeamMember는 존재하지 않는 멤버 ID로 시도 시 예외를 발생시켜야 한다") {
        // Given
        val teamId = 1L
        val teamMemberId = 2L
        val nonExistentMemberId = 999L
        val invalidMemberDetails = MemberDetails(nonExistentMemberId, "password", "invalid@example.com", "Invalid")

        every { memberRepository.findById(nonExistentMemberId) } returns Optional.empty()

        // When/Then
        val exception = shouldThrow<CustomLogicException> {
            teamMemberService.removeTeamMember(invalidMemberDetails, teamId, teamMemberId)
        }

        exception.exceptionCode shouldBe ExceptionCode.MEMBER_ID_NOTFOUND
    }

    test("removeTeamMember는 존재하지 않는 팀 ID로 시도 시 예외를 발생시켜야 한다") {
        // Given
        val nonExistentTeamId = 999L
        val teamMemberId = 2L

        every { memberRepository.findById(leader.id!!) } returns Optional.of(leader)
        every { teamRepository.findById(nonExistentTeamId) } returns Optional.empty()

        // When/Then
        val exception = shouldThrow<CustomLogicException> {
            teamMemberService.removeTeamMember(leaderDetails, nonExistentTeamId, teamMemberId)
        }

        exception.exceptionCode shouldBe ExceptionCode.TEAM_NOT_FOUND
    }

    test("removeTeamMember는 존재하지 않는 팀 멤버 ID로 시도 시 예외를 발생시켜야 한다") {
        // Given
        val teamId = 1L
        val nonExistentTeamMemberId = 999L

        every { memberRepository.findById(leader.id!!) } returns Optional.of(leader)
        every { teamRepository.findById(teamId) } returns Optional.of(team)
        every { teamMemberRepository.findById(nonExistentTeamMemberId) } returns Optional.empty()

        // When/Then
        val exception = shouldThrow<CustomLogicException> {
            teamMemberService.removeTeamMember(leaderDetails, teamId, nonExistentTeamMemberId)
        }

        exception.exceptionCode shouldBe ExceptionCode.TEAM_MEMBER_NOT_FOUND
    }

    test("removeTeamMember는 다른 팀의 멤버를 삭제하려고 할 때 예외를 발생시켜야 한다") {
        // Given
        val teamId = 1L
        val otherTeamId = 2L
        val teamMemberId = 2L

        val otherTeam = Team(
            id = otherTeamId,
            name = "다른 팀",
            teamCode = "GHIJKL",
            leader = leader,
            teamPassword = "password123"
        )

        val otherTeamMember = TeamMember(
            id = teamMemberId,
            member = member,
            team = otherTeam
        )

        every { memberRepository.findById(leader.id!!) } returns Optional.of(leader)
        every { teamRepository.findById(teamId) } returns Optional.of(team)
        every { teamMemberRepository.findById(teamMemberId) } returns Optional.of(otherTeamMember)

        // When/Then
        val exception = shouldThrow<CustomLogicException> {
            teamMemberService.removeTeamMember(leaderDetails, teamId, teamMemberId)
        }

        exception.exceptionCode shouldBe ExceptionCode.NOT_TEAM_MEMBER
    }

    test("removeTeamMember는 팀 리더가 아닌 멤버가 삭제를 시도할 때 예외를 발생시켜야 한다") {
        // Given
        val teamId = 1L
        val teamMemberId = 2L
        val memberDetails = MemberDetails(member)

        every { memberRepository.findById(member.id!!) } returns Optional.of(member)
        every { teamRepository.findById(teamId) } returns Optional.of(team)
        every { teamMemberRepository.findById(teamMemberId) } returns Optional.of(teamMember)

        // When/Then
        val exception = shouldThrow<CustomLogicException> {
            teamMemberService.removeTeamMember(memberDetails, teamId, teamMemberId)
        }

        exception.exceptionCode shouldBe ExceptionCode.ROLE_FORBIDDEN
    }

    test("removeTeamMember는 팀 리더를 삭제하려고 할 때 예외를 발생시켜야 한다") {
        // Given
        val teamId = 1L
        val leaderTeamMemberId = 1L

        every { memberRepository.findById(leader.id!!) } returns Optional.of(leader)
        every { teamRepository.findById(teamId) } returns Optional.of(team)
        every { teamMemberRepository.findById(leaderTeamMemberId) } returns Optional.of(leaderTeamMember)

        // When/Then
        val exception = shouldThrow<CustomLogicException> {
            teamMemberService.removeTeamMember(leaderDetails, teamId, leaderTeamMemberId)
        }

        exception.exceptionCode shouldBe ExceptionCode.METHOD_NOT_ALLOWED
        exception.message shouldBe "허용되지 않은 메서드입니다."
    }
})
