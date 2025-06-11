package com.luckyseven.backend.domain.budget.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.luckyseven.backend.domain.budget.entity.CurrencyCode;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

public record BudgetCreateRequest (
    @NotNull
    @JsonProperty("totalAmount")
    @DecimalMin(value = "0.0", inclusive = false)
    BigDecimal totalAmount,

    @NotNull
    @JsonProperty("isExchanged")
    Boolean isExchanged,

    @JsonProperty("exchangeRate")
    @DecimalMin(value = "0.0", inclusive = false)
    BigDecimal exchangeRate,

    @NotNull
    @JsonProperty("foreignCurrency")
    CurrencyCode foreignCurrency
) { }
