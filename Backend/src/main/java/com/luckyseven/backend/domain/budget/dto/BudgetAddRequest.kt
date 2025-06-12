package com.luckyseven.backend.domain.budget.dto

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotNull
import java.math.BigDecimal


data class BudgetAddRequest(

    @field:JsonProperty("additionalBudget")
    @field:NotNull
    @field:DecimalMin(value = "0.0", inclusive = false)
    val additionalBudget: BigDecimal,

    @field:JsonProperty("isExchanged")
    @field:NotNull
    val isExchanged: Boolean,

    @field:JsonProperty("exchangeRate")
    @field:DecimalMin(value = "0.0", inclusive = false)
    val exchangeRate: BigDecimal?

)
