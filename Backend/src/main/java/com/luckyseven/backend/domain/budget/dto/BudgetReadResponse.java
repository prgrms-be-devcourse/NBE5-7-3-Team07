package com.luckyseven.backend.domain.budget.dto;

import com.luckyseven.backend.domain.budget.entity.CurrencyCode;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

public record BudgetReadResponse (
    @NotNull Long id,
    @NotNull LocalDateTime updatedAt,

    @NotNull Long setBy,
    @NotNull BigDecimal totalAmount,
    @NotNull BigDecimal balance,
    @NotNull CurrencyCode foreignCurrency,

    BigDecimal avgExchangeRate,
    BigDecimal foreignBalance
) { }
