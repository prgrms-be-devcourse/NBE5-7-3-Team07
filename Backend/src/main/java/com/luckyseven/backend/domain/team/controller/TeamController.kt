package com.luckyseven.backend.domain.team.controller

import com.luckyseven.backend.domain.member.service.utill.MemberDetails
import com.luckyseven.backend.domain.team.dto.*
import com.luckyseven.backend.domain.team.service.TeamMemberService
import com.luckyseven.backend.domain.team.service.TeamService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import lombok.RequiredArgsConstructor
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.ErrorResponse
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/teams")
@RequiredArgsConstructor
@Tag(name = "팀 관리", description = "팀 도메인 API")
class TeamController(
    val teamService: TeamService,
    val teamMemberService: TeamMemberService
) {

    @Operation(summary = "팀 생성", description = "새로운 팀을 생성합니다")
    @ApiResponse(
        responseCode = "201",
        description = "팀 생성 성공",
        content = [Content(schema = Schema(implementation = TeamCreateResponse::class))]
    )
    @ApiResponse(
        responseCode = "400",
        description = "잘못된 요청 데이터",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))]
    )
    @ApiResponse(
        responseCode = "401",
        description = "인증되지 않은 사용자",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))]
    )
    @ApiResponse(
        responseCode = "409",
        description = "이미 존재하는 팀 이름",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))]
    )
    @PostMapping
    fun createTeam(
        @Parameter(hidden = true) @AuthenticationPrincipal memberDetails: MemberDetails,
        @Parameter(description = "팀 생성 요청 정보") @RequestBody request: @Valid TeamCreateRequest
    ): ResponseEntity<TeamCreateResponse?> {
        val response = teamService.createTeam(memberDetails, request)
        return ResponseEntity.ok<TeamCreateResponse?>(response)
    }

    @Operation(summary = "팀 참가", description = "유저가 존재하는 팀에 참가합니다")
    @ApiResponse(
        responseCode = "200",
        description = "팀 참가 성공",
        content = [Content(schema = Schema(implementation = TeamJoinResponse::class))]
    )
    @ApiResponse(
        responseCode = "400",
        description = "잘못된 요청 데이터 또는 비밀번호",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))]
    )
    @ApiResponse(
        responseCode = "401",
        description = "인증되지 않은 사용자",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))]
    )
    @ApiResponse(
        responseCode = "404",
        description = "존재하지 않는 팀",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))]
    )
    @ApiResponse(
        responseCode = "409",
        description = "이미 팀에 가입된 사용자",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))]
    )
    @PostMapping("/members")
    fun joinTeam(
        @AuthenticationPrincipal memberDetails: MemberDetails,
        @Parameter(description = "팀 참가 요청 정보") @RequestBody request: @Valid TeamJoinRequest
    ): ResponseEntity<TeamJoinResponse?> {
        // Service
        val response = teamService.joinTeam(
            memberDetails, request.teamCode!!,
            request.teamPassword
        )

        return ResponseEntity.ok<TeamJoinResponse?>(response)
    }

    @GetMapping("/myTeams")
    @Operation(summary = "내 팀 목록 조회", description = "로그인한 사용자가 속한 모든 팀 목록을 조회합니다")
    @ApiResponse(
        responseCode = "200",
        description = "팀 목록 조회 성공",
        content = [Content(schema = Schema(implementation = TeamListResponse::class))]
    )
    @ApiResponse(
        responseCode = "401",
        description = "인증되지 않은 사용자",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))]
    )
    fun getMyTeams(
        @Parameter(hidden = true) @AuthenticationPrincipal memberDetails: MemberDetails
    ): ResponseEntity<List<TeamListResponse>> {
        val teams: List<TeamListResponse> =
            teamService.getTeamsByMemberId(memberDetails.id)
        return ResponseEntity.ok(teams)
    }

    @Operation(summary = "팀 멤버 조회", description = "팀의 모든 멤버를 조회합니다")
    @ApiResponse(
        responseCode = "200",
        description = "조회 성공",
        content = [Content(schema = Schema(implementation = TeamMemberDto::class))]
    )
    @ApiResponse(
        responseCode = "400",
        description = "잘못된 팀 ID",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))]
    )
    @ApiResponse(
        responseCode = "401",
        description = "인증되지 않은 사용자",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))]
    )
    @ApiResponse(
        responseCode = "403",
        description = "팀 멤버가 아닌 사용자의 접근",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))]
    )
    @ApiResponse(
        responseCode = "404",
        description = "존재하지 않는 팀",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))]
    )
    @GetMapping("/{teamId}/members")
    fun getTeamMembers(@PathVariable teamId: Long): ResponseEntity<List<TeamMemberDto>> {
        val teamMembers: List<TeamMemberDto> =
            teamMemberService.getTeamMemberByTeamId(teamId)
        return ResponseEntity.ok(teamMembers)
    }

    @Operation(summary = "팀 삭제", description = "팀의 멤버를 삭제합니다")
    @ApiResponse(responseCode = "204", description = "멤버 삭제 성공")
    @ApiResponse(
        responseCode = "400",
        description = "잘못된 요청 또는 팀 리더 삭제 시도",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))]
    )
    @ApiResponse(
        responseCode = "401",
        description = "인증되지 않은 사용자",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))]
    )
    @ApiResponse(
        responseCode = "403",
        description = "권한 없는 작업 시도 (팀 리더만 삭제 가능)",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))]
    )
    @ApiResponse(
        responseCode = "404",
        description = "존재하지 않는 팀 또는 팀 멤버",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))]
    )
    @DeleteMapping("/{teamId}/members/{teamMemberId}")
    fun removeTeamMember(
        @AuthenticationPrincipal memberDetails: MemberDetails,
        @PathVariable teamMemberId: Long,
        @PathVariable teamId: Long
    ): ResponseEntity<Void> {
        teamMemberService.removeTeamMember(memberDetails, teamId, teamMemberId)
        return ResponseEntity.noContent().build()
    }

    @Operation(summary = "팀 대시보드를 조회", description = "팀 대시보드를 조회합니다")
    @ApiResponse(
        responseCode = "200",
        description = "대시보드 조회 성공",
        content = [Content(schema = Schema(implementation = TeamDashboardResponse::class))]
    )
    @ApiResponse(
        responseCode = "400",
        description = "잘못된 팀 ID",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))]
    )
    @ApiResponse(
        responseCode = "401",
        description = "인증되지 않은 사용자",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))]
    )
    @ApiResponse(
        responseCode = "403",
        description = "팀 멤버가 아닌 사용자의 접근",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))]
    )
    @ApiResponse(
        responseCode = "404",
        description = "존재하지 않는 팀",
        content = [Content(schema = Schema(implementation = ErrorResponse::class))]
    )
    @GetMapping("/{teamId}/dashboard")
    fun getTeamDashboard(@PathVariable teamId: Long): ResponseEntity<TeamDashboardResponse?> {
        val dashboardResponse = teamService.getTeamDashboard(teamId)

        return ResponseEntity.ok(dashboardResponse)
    }
}

