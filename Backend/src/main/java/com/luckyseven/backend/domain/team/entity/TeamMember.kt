package com.luckyseven.backend.domain.team.entity

import com.luckyseven.backend.domain.member.entity.Member
import com.luckyseven.backend.sharedkernel.entity.BaseEntity
import jakarta.persistence.*
import lombok.*

@Entity
@Table(
    name = "team_member",
    indexes = [Index(name = "idx_tm_member", columnList = "member_id"), Index(
        name = "idx_tm_team",
        columnList = "team_id"
    )]
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@AttributeOverride(name = "id", column = Column(name = "team_member_id"))
class TeamMember(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "team_id",
        nullable = false,
        foreignKey = ForeignKey(ConstraintMode.NO_CONSTRAINT)
    )
    var team: Team? = null,


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "member_id",
        nullable = false,
        foreignKey = ForeignKey(ConstraintMode.NO_CONSTRAINT)
    )
    var member: Member? = null,

    ) : BaseEntity()
