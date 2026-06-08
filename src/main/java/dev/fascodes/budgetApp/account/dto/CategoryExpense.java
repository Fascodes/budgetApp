package dev.fascodes.budgetApp.account.dto;

import java.math.BigDecimal;

public class CategoryExpense {
    private String categoryName;
    private BigDecimal total;

    public CategoryExpense(String categoryName, BigDecimal total) {
        this.categoryName = categoryName;
        this.total = total;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public BigDecimal getTotal() {
        return total;
    }
}
