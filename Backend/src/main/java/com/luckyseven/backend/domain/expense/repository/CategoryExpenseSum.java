package com.luckyseven.backend.domain.expense.repository;

import com.luckyseven.backend.domain.expense.enums.ExpenseCategory;
import java.math.BigDecimal;

public interface CategoryExpenseSum {
  ExpenseCategory getCategory();
  BigDecimal getTotalAmount();
}
