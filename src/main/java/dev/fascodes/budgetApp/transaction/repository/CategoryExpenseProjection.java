package dev.fascodes.budgetApp.transaction.repository;

import java.math.BigDecimal;

public interface CategoryExpenseProjection {
    String getCategoryName();
    BigDecimal getTotal();
}
