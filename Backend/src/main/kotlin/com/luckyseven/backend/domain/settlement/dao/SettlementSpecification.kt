package com.luckyseven.backend.domain.settlement.dao

import com.luckyseven.backend.domain.expense.entity.Expense
import com.luckyseven.backend.domain.member.entity.Member
import com.luckyseven.backend.domain.settlement.dto.SettlementSearchCondition
import com.luckyseven.backend.domain.settlement.entity.Settlement
import com.luckyseven.backend.domain.team.entity.Team
import org.springframework.data.jpa.domain.Specification

object SettlementSpecification {
    fun hasTeamId(teamId: Long?): Specification<Settlement> = Specification { root, _, cb ->
        if (teamId == null) null
        else cb.equal(root.get<Expense>("expense").get<Team>("team").get<Long>("id"), teamId)
    }

    fun hasPayerId(payerId: Long?): Specification<Settlement> = Specification { root, _, cb ->
        if (payerId == null) null
        else cb.equal(root.get<Member>("payer").get<Long>("id"), payerId)
    }

    fun hasSettlerId(settlerId: Long?): Specification<Settlement> = Specification { root, _, cb ->
        if (settlerId == null) null
        else cb.equal(root.get<Member>("settler").get<Long>("id"), settlerId)
    }

    fun hasExpenseId(expenseId: Long?): Specification<Settlement> = Specification { root, _, cb ->
        if (expenseId == null) null
        else cb.equal(root.get<Expense>("expense").get<Long>("id"), expenseId)
    }

    fun isSettled(isSettled: Boolean?): Specification<Settlement> = Specification { root, _, cb ->
        if (isSettled == null) null
        else cb.equal(root.get<Boolean>("isSettled"), isSettled)
    }

    fun createSpecification(
        teamId: Long?,
        condition: SettlementSearchCondition
    ): Specification<Settlement> = Specification
        .where(hasTeamId(teamId))
        .and(hasPayerId(condition.payerId))
        .and(hasSettlerId(condition.settlerId))
        .and(hasExpenseId(condition.expenseId))
        .and(isSettled(condition.isSettled))
}
