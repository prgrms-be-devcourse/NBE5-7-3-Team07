package com.luckyseven.backend.domain.budget.dto;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

public record BudgetCreateResponse (
    @NotNull Long id,
    @NotNull LocalDateTime createdAt,

    @NotNull Long setBy,
    @NotNull BigDecimal balance,

    @NotNull BigDecimal avgExchangeRate,
    @NotNull BigDecimal foreignBalance
) { }
