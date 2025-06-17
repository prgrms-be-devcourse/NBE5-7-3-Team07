package com.luckyseven.backend.domain.settlement.dto

import java.math.BigDecimal

data class SettlementAggregationResponse(
    val aggregations: List<SettlementMemberAggregationResponse>
)

data class SettlementMemberAggregationResponse(
    val from: Long,
    val to: Long,
    val amount: BigDecimal
)
