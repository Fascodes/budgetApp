package dev.fascodes.budgetApp.account.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

public class AccountRequest {
    @NotBlank(message = "Account name is required")
    private String name;

    @DecimalMin(value = "0.00", message = "Balance cannot be negative")
    private BigDecimal balance;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }
}
