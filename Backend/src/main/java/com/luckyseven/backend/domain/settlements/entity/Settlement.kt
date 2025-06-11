package com.luckyseven.backend.domain.settlements.entity

import com.luckyseven.backend.domain.expense.entity.Expense
import com.luckyseven.backend.domain.member.entity.Member
import com.luckyseven.backend.sharedkernel.entity.BaseEntity
import jakarta.persistence.*
import java.math.BigDecimal

@Entity
@Table(
    indexes = [
        Index(name = "idx_settler", columnList = "settler_id"),  // settler에 대한 인덱스
        Index(name = "idx_payer", columnList = "payer_id"),      // payer에 대한 인덱스
        Index(name = "idx_expense", columnList = "expense_id")   // expense에 대한 인덱스
    ]
)
class Settlement(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false, precision = 15, scale = 2)
    var amount: BigDecimal = BigDecimal.ZERO,

    @Column(nullable = false)
    var isSettled: Boolean = false,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        foreignKey = ForeignKey(ConstraintMode.NO_CONSTRAINT),
        name = "settler_id",
        nullable = false
    )
    var settler: Member,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        foreignKey = ForeignKey(ConstraintMode.NO_CONSTRAINT),
        name = "payer_id",
        nullable = false
    )
    var payer: Member,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        foreignKey = ForeignKey(ConstraintMode.NO_CONSTRAINT),
        name = "expense_id",
        nullable = false
    )
    var expense: Expense
) : BaseEntity() {

    fun update(
        amount: BigDecimal? = null,
        settler: Member? = null,
        payer: Member? = null,
        expense: Expense? = null,
        isSettled: Boolean? = null
    ) {
        amount?.let { this.amount = it }
        settler?.let { this.settler = it }
        payer?.let { this.payer = it }
        expense?.let { this.expense = it }
        isSettled?.let { this.isSettled = it }
    }

    fun convertSettled() {
        this.isSettled = !this.isSettled
    }
}