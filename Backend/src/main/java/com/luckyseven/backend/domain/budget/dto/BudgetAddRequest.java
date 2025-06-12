package com.luckyseven.backend.domain.budget.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.DecimalMin;
import java.math.BigDecimal;

public record BudgetAddRequest(
    @JsonProperty("additionalBudget")
    @DecimalMin(value = "0.0", inclusive = false)
    BigDecimal additionalBudget,

    @JsonProperty("isExchanged")
    Boolean isExchanged,

    @JsonProperty("exchangeRate")
    @DecimalMin(value = "0.0", inclusive = false)
    BigDecimal exchangeRate
) { }
