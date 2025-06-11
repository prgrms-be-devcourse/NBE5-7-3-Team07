package com.luckyseven.backend.domain.expense.entity

import com.luckyseven.backend.domain.expense.enums.ExpenseCategory
import com.luckyseven.backend.domain.expense.enums.PaymentMethod
import com.luckyseven.backend.domain.member.entity.Member
import com.luckyseven.backend.domain.team.entity.Team
import com.luckyseven.backend.sharedkernel.entity.BaseEntity
import jakarta.persistence.*
import java.math.BigDecimal

@Entity
@Table(
    name = "expense",
    indexes = [
        Index(name = "idx_expense_payer", columnList = "payer_id"),
        Index(name = "idx_team", columnList = "team_id")
    ]
)
class Expense(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "expense_id", nullable = false)
    var id: Long? = null,

    @Column(nullable = false)
    var description: String,

    @Column(nullable = false, precision = 15, scale = 2)
    var amount: BigDecimal,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var category: ExpenseCategory,

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    var paymentMethod: PaymentMethod,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "payer_id",
        foreignKey = ForeignKey(ConstraintMode.NO_CONSTRAINT),
        nullable = false
    )
    var payer: Member,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "team_id",
        foreignKey = ForeignKey(ConstraintMode.NO_CONSTRAINT),
        nullable = false
    )
    var team: Team

) : BaseEntity() {

    fun update(description: String?, amount: BigDecimal?, category: ExpenseCategory?) {
        description?.let { this.description = it }
        amount?.let { this.amount = it }
        category?.let { this.category = it }
    }
}
