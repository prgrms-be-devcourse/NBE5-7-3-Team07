package com.luckyseven.backend.domain.budget.dto;

import com.luckyseven.backend.domain.budget.entity.CurrencyCode;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

public record BudgetUpdateResponse (
    @NotNull Long id,
    @NotNull LocalDateTime updatedAt,

    @NotNull Long setBy,
    @NotNull BigDecimal balance,
    @NotNull CurrencyCode foreignCurrency,

    @NotNull BigDecimal avgExchangeRate,
    @NotNull BigDecimal foreignBalance
) { }
