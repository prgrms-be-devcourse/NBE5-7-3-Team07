package com.luckyseven.backend.domain.team.service

import com.luckyseven.backend.domain.member.repository.MemberRepository
import com.luckyseven.backend.domain.member.service.utill.MemberDetails
import com.luckyseven.backend.domain.team.dto.TeamMemberDto
import com.luckyseven.backend.domain.team.entity.TeamMember
import com.luckyseven.backend.domain.team.repository.TeamMemberRepository
import com.luckyseven.backend.domain.team.repository.TeamRepository
import com.luckyseven.backend.domain.team.util.TeamMemberMapper
import com.luckyseven.backend.sharedkernel.exception.CustomLogicException
import com.luckyseven.backend.sharedkernel.exception.ExceptionCode
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class TeamMemberService(
    val teamRepository: TeamRepository,
    val teamMemberRepository: TeamMemberRepository,
    val memberRepository: MemberRepository,
) {


    @Transactional(readOnly = true)
    fun getTeamMemberByTeamId(id: Long): List<TeamMemberDto> {
        if (!teamRepository.existsById(id)) {
            throw CustomLogicException(
                ExceptionCode.TEAM_NOT_FOUND,
                "ID가 [%d]인 팀을 찾을 수 없습니다", id
            )
        }
        val teamMembers: List<TeamMember> = teamMemberRepository.findByTeamId(id)
        return TeamMemberMapper.toDtoList(teamMembers)
    }

    @Transactional
    fun removeTeamMember(memberDetails: MemberDetails, teamId: Long, teamMemberId: Long) {
        val memberId = memberDetails.id
        val member = memberRepository.findById(memberId)
            .orElseThrow {
                CustomLogicException(
                    ExceptionCode.MEMBER_ID_NOTFOUND,
                    memberId
                )
            }

        val team = teamRepository.findById(teamId)
            .orElseThrow { CustomLogicException(ExceptionCode.TEAM_NOT_FOUND) }

        // 팀 멤버 존재 여부 확인
        val teamMember = teamMemberRepository.findById(teamMemberId)
            .orElseThrow { CustomLogicException(ExceptionCode.TEAM_MEMBER_NOT_FOUND) }

        if (teamMember?.team?.id != teamId) {
            throw CustomLogicException(
                ExceptionCode.NOT_TEAM_MEMBER,
                "팀 멤버 ID [%d]는 팀 ID [%d]에 속한 멤버가 아닙니다", teamMemberId, teamId
            )
        }

        if (team?.leader?.id != member?.id) {
            throw CustomLogicException(
                ExceptionCode.ROLE_FORBIDDEN,
                "팀 멤버를 삭제할 권한이 없습니다. 팀 리더만 멤버를 삭제할 수 있습니다."
            )
        }
        if (team?.leader?.id == teamMember.member?.id) {
            throw CustomLogicException(ExceptionCode.METHOD_NOT_ALLOWED, "팀 리더는 삭제 불가능합니다.")
        }
        // 팀 멤버 삭제
        team?.removeTeamMember(teamMember)
        teamMemberRepository.deleteById(teamMemberId)
    }
}
