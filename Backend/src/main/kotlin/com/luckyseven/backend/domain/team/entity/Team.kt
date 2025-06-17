package com.luckyseven.backend.domain.team.entity

import com.fasterxml.jackson.annotation.JsonIgnore
import com.luckyseven.backend.domain.budget.entity.Budget
import com.luckyseven.backend.domain.member.entity.Member
import com.luckyseven.backend.domain.team.enums.TeamStatus
import com.luckyseven.backend.sharedkernel.entity.BaseEntity
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(
    name = "team",
    indexes = [
        Index(
            name = "idx_team_leader_id",
            columnList = "leader_id"
        ),
        Index(
            name = "idx_team_budget_id",
            columnList = "budget_id"
        )
    ]
)
@AttributeOverride(name = "id", column = Column(name = "team_id"))
class Team(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    var name: String,

    @Column(nullable = false, unique = true)
    var teamCode: String,

    @Column(nullable = false)
    var teamPassword: String,

    @Enumerated(EnumType.STRING)
    var status: TeamStatus = TeamStatus.ACTIVE,

    var deletionScheduledAt: LocalDateTime? = null,


    /**
     * 팀장 ID
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "leader_id",
        nullable = false,
        foreignKey = ForeignKey(value = ConstraintMode.NO_CONSTRAINT)
    )
    var leader: Member,

    /**
     * 팀의 예산 정보
     */
    @OneToOne(cascade = [CascadeType.ALL], orphanRemoval = true)
    @JoinColumn(
        name = "budget_id",
        unique = true,
        foreignKey = ForeignKey(value = ConstraintMode.NO_CONSTRAINT)
    )
    var budget: Budget? = null,

    @OneToMany(mappedBy = "team", cascade = [CascadeType.ALL], orphanRemoval = true)
    @JsonIgnore
    @Schema(hidden = true)
    val teamMembers: MutableList<TeamMember> = mutableListOf()
) : BaseEntity() {

    fun addTeamMember(teamMember: TeamMember) {
        teamMembers.add(teamMember)
        teamMember.team = this
    }

    fun removeTeamMember(teamMember: TeamMember) {
        teamMembers.remove(teamMember)
        teamMember.team = null
    }

}
