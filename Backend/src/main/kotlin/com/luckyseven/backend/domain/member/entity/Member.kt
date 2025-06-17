package com.luckyseven.backend.domain.member.entity

import com.fasterxml.jackson.annotation.JsonIgnore
import com.luckyseven.backend.domain.team.entity.Team
import com.luckyseven.backend.domain.team.entity.TeamMember
import com.luckyseven.backend.sharedkernel.entity.BaseEntity
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.persistence.*

@Entity
@Table(name = "member", indexes = [
    Index(name = "idx_member_email", columnList = "email"),
    Index(name = "idx_member_nickname", columnList = "nickname")
])
class Member(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(length = 255, nullable = false)
    val email: String,

    @Column(length = 255, nullable = false)
    val password: String,

    @Column(length = 50, nullable = false)
    val nickname: String,

    @OneToMany(mappedBy = "member", cascade = [CascadeType.ALL], orphanRemoval = true)
    @JsonIgnore
    @Schema(hidden = true)
    open var teamMembers: MutableList<TeamMember> = mutableListOf(),

    @OneToMany(mappedBy = "leader")
    @JsonIgnore
    @Schema(hidden = true)
    open var leadingTeams: MutableList<Team> = mutableListOf()

) : BaseEntity() {
    fun addLeadingTeam(team: Team) {
        leadingTeams.add(team)
        if (team.leader != this) {
            team.leader = this
        }
    }

    fun removeLeadingTeam(team: Team) {
        leadingTeams.remove(team)
        if (team.leader == this) {
            false
        }
    }

    fun addTeamMember(teamMember: TeamMember) {
        teamMembers.add(teamMember)
        teamMember.member = this
    }
}