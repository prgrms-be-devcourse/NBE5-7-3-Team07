package com.luckyseven.backend.domain.budget.dto

import com.fasterxml.jackson.annotation.JsonProperty
import com.luckyseven.backend.domain.budget.entity.CurrencyCode
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

data class BudgetCreateRequest(

    @field:JsonProperty("totalAmount")
    @field:NotNull
    @field:DecimalMin(value = "0.0", inclusive = false)
    val totalAmount: BigDecimal,

    @field:JsonProperty("isExchanged")
    @field:NotNull
    val isExchanged: Boolean,

    @field:JsonProperty("exchangeRate")
    @field:DecimalMin(value = "0.0", inclusive = false)
    val exchangeRate: BigDecimal?,

    @field:JsonProperty("foreignCurrency")
    @field:NotNull
    val foreignCurrency: CurrencyCode
)

data class BudgetUpdateRequest(
    @field:JsonProperty("totalAmount")
    @field:NotNull
    @field:DecimalMin(value = "0.0", inclusive = false)
    val totalAmount: BigDecimal,

    @field:JsonProperty("isExchanged")
    @field:NotNull
    val isExchanged: Boolean,

    @field:JsonProperty("exchangeRate")
    @field:DecimalMin(value = "0.0", inclusive = false)
    val exchangeRate: BigDecimal?
)