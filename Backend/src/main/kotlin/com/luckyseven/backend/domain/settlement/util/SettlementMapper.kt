package com.luckyseven.backend.domain.settlement.util

import com.luckyseven.backend.domain.expense.entity.Expense
import com.luckyseven.backend.domain.member.entity.Member
import com.luckyseven.backend.domain.settlement.dto.SettlementCreateRequest
import com.luckyseven.backend.domain.settlement.dto.SettlementResponse
import com.luckyseven.backend.domain.settlement.entity.Settlement
import java.math.BigDecimal

object SettlementMapper {

    fun toSettlementResponse(settlement: Settlement): SettlementResponse {
        return SettlementResponse(
            id = settlement.id,
            amount = settlement.amount,
            createdAt = settlement.createdAt!!,
            updatedAt = settlement.updatedAt!!,
            isSettled = settlement.isSettled,
            settlerId = settlement.settler.id!!,
            settlerNickName = settlement.settler.nickname,
            payerId = settlement.payer.id!!,
            payerNickName = settlement.payer.nickname,
            expenseId = settlement.expense.id!!,
            expenseDescription = settlement.expense.description,
            teamId = settlement.expense.team.id!!
        )
    }

    fun fromSettlementCreateRequest(
        request: SettlementCreateRequest,
        settler: Member,
        payer: Member,
        expense: Expense
    ): Settlement {
        return Settlement(
            amount = request.amount,
            settler = settler,
            payer = payer,
            expense = expense
        )
    }

    fun toSettlementCreateRequest(
        expense: Expense,
        payerId: Long,
        settlerId: Long,
        shareAmount: BigDecimal
    ): SettlementCreateRequest {
        return SettlementCreateRequest(
            expenseId = expense.id!!,
            payerId = payerId,
            settlerId = settlerId,
            amount = shareAmount
        )
    }
}
