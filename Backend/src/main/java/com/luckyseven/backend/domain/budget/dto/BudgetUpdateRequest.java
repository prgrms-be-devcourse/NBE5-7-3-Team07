package com.luckyseven.backend.domain.budget.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.luckyseven.backend.domain.budget.entity.CurrencyCode;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

public record BudgetUpdateRequest (
    @JsonProperty("totalAmount")
    @DecimalMin(value = "0.0", inclusive = false)
    BigDecimal totalAmount,

    @JsonProperty("isExchanged")
    Boolean isExchanged,

    @JsonProperty("exchangeRate")
    @DecimalMin(value = "0.0", inclusive = false)
    BigDecimal exchangeRate,


    @JsonProperty("additionalBudget")
    @DecimalMin(value = "0.0", inclusive = false)
    BigDecimal additionalBudget
) { }
