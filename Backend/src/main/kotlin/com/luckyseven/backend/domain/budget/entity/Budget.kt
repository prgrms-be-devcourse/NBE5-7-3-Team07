package com.luckyseven.backend.domain.budget.entity

import com.luckyseven.backend.domain.team.entity.Team
import com.luckyseven.backend.sharedkernel.entity.BaseEntity
import com.luckyseven.backend.sharedkernel.exception.CustomLogicException
import com.luckyseven.backend.sharedkernel.exception.ExceptionCode
import jakarta.persistence.*
import java.math.BigDecimal
import java.math.RoundingMode

@Entity
@Table(
    indexes = [
        Index(name = "idx_budget_team", columnList = "team_id")
    ]
)
class Budget(

    @Id
    @Column(name = "budget_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @OneToOne(mappedBy = "budget", fetch = FetchType.LAZY)
    var team: Team? = null,

    totalAmount: BigDecimal,

    @Column(nullable = false)
    var setBy: Long,

    @Column(nullable = false)
    var balance: BigDecimal,

    @Column
    var foreignBalance: BigDecimal? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 3)
    val foreignCurrency: CurrencyCode,

    @Column
    var avgExchangeRate: BigDecimal? = null

) : BaseEntity() {

    companion object {
        private const val SCALE = 2
        private val ROUNDING: RoundingMode = RoundingMode.HALF_UP
        private val ZERO = BigDecimal.ZERO
    }

    @Column(nullable = false)
    var totalAmount: BigDecimal = totalAmount
        set(value) {
            balance = balance.add(value.subtract(field))
            field = value
        }

    fun setExchangeInfo(isExchanged: Boolean, amount: BigDecimal, exchangeRate: BigDecimal?) {
        if (!isExchanged) {
            this.avgExchangeRate = null
            this.foreignBalance = null
            return
        }

        updateForeignBalance(amount, exchangeRate!!)
    }

    fun updateExchangeInfo(isExchanged: Boolean, amount: BigDecimal, exchangeRate: BigDecimal?) {
        if (!isExchanged) {
            return
        }

        // isExchanged가 true면 exchangeRate를 반드시 포함
        updateAvgExchangeRate(amount, exchangeRate!!)
        updateForeignBalance(amount, exchangeRate!!)
    }

    // 예산 추가 후 외화잔고 및 평균환율 수정
    private fun updateAvgExchangeRate(amount: BigDecimal, exchangeRate: BigDecimal) {
        if (this.avgExchangeRate == null || this.avgExchangeRate!!.compareTo(ZERO) == 0) {
            avgExchangeRate = exchangeRate
            return
        }
        val foreignAmount = amount.divide(
            exchangeRate, 10,
            ROUNDING
        ) // 외화 환산, 충분한 정밀도 확보
        val totalCost = this.foreignBalance!!.multiply(this.avgExchangeRate).add(amount)
        val totalForeign = this.foreignBalance!!.add(foreignAmount)
        this.avgExchangeRate = totalCost.divide(totalForeign, SCALE, ROUNDING)
    }

    private fun updateForeignBalance(amount: BigDecimal, exchangeRate: BigDecimal) {
        val additionalBudget = amount.divide(exchangeRate, SCALE, ROUNDING)
        if (this.foreignBalance == null) {
            foreignBalance = ZERO
        }
        this.foreignBalance = this.foreignBalance!!.add(additionalBudget)
        this.avgExchangeRate = exchangeRate
    }

    fun setForeignBalance() {
        if (avgExchangeRate != null) {
            this.foreignBalance = balance.divide(avgExchangeRate, SCALE, ROUNDING)
        }
    }

    fun setTeam(team: Team?): Budget {
        // 기존 연결 해제
        this.team?.budget = null

        this.team = team

        // 새로운 연결 설정 (Team이 null이 아닌 경우)
        if (team != null && team.budget !== this) {
            team.budget = this
        }

        return this
    }

    fun debitKrw(krwAmount: BigDecimal) {
        validateSufficientBalance(krwAmount, balance)
        balance = balance.subtract(krwAmount)
    }

    fun debitForeign(foreignAmount: BigDecimal) {
        val krwAmount = foreignAmount.multiply(avgExchangeRate)
        validateSufficientBalance(krwAmount, balance)
        validateSufficientBalance(foreignAmount, foreignBalance!!)
        balance = balance.subtract(krwAmount)
        foreignBalance = foreignBalance!!.subtract(foreignAmount)
    }

    fun creditKrw(krwAmount: BigDecimal) {
        balance = balance.add(krwAmount)
    }

    fun creditForeign(foreignAmount: BigDecimal) {
        val krwAmount = foreignAmount.multiply(avgExchangeRate)
        this.balance = this.balance.add(krwAmount)

        if (this.foreignBalance == null) {
            this.foreignBalance = ZERO
        }

        this.foreignBalance = this.foreignBalance!!.add(foreignAmount)
    }


    private fun validateSufficientBalance(amount: BigDecimal?, current: BigDecimal) {
        if (current < amount) {
            throw CustomLogicException(ExceptionCode.INSUFFICIENT_BALANCE)
        }
    }
}
