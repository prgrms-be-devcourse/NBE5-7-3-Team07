package com.luckyseven.backend.domain.expense.dto;

import com.luckyseven.backend.domain.expense.enums.ExpenseCategory;
import com.luckyseven.backend.domain.expense.enums.PaymentMethod;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;
import lombok.Builder;

@Builder
public record ExpenseRequest(
    @NotBlank(message = "설명은 공백일 수 없습니다.")
    String description,

    @NotNull(message = "금액은 필수 입력 항목입니다.")
    @DecimalMin(value = "0.00", message = "금액은 0 이상이어야 합니다.")
    BigDecimal amount,

    @NotNull(message = "카테고리는 필수 선택 항목입니다.")
    ExpenseCategory category,

    @NotNull(message = "결제자는 필수 선택 항목입니다.")
    Long payerId,

    @NotNull(message = "결제 수단은 필수 선택 항목입니다.")
    PaymentMethod paymentMethod,

    @NotEmpty(message = "정산 대상자가 최소 1명 이상 필요합니다.")
    List<@NotNull(message = "정산 대상자 ID는 null일 수 없습니다.") Long> settlerId
) {

}
