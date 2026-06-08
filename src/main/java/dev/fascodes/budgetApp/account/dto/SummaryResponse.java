package dev.fascodes.budgetApp.account.dto;

import java.math.BigDecimal;
import java.util.List;

public class SummaryResponse {
    private BigDecimal totalIncome;
    private BigDecimal totalExpenses;
    private List<CategoryExpense> expensesByCategory;

    public SummaryResponse(BigDecimal totalIncome, BigDecimal totalExpenses, List<CategoryExpense> expensesByCategory) {
        this.totalIncome = totalIncome;
        this.totalExpenses = totalExpenses;
        this.expensesByCategory = expensesByCategory;
    }

    public BigDecimal getTotalIncome() {
        return totalIncome;
    }

    public BigDecimal getTotalExpenses() {
        return totalExpenses;
    }

    public List<CategoryExpense> getExpensesByCategory() {
        return expensesByCategory;
    }
}
