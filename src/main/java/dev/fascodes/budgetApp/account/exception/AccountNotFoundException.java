package dev.fascodes.budgetApp.account.exception;

import dev.fascodes.budgetApp.common.ResourceNotFoundException;

public class AccountNotFoundException extends ResourceNotFoundException {
    public AccountNotFoundException(Long id) {
        super("Account not found with id: " + id);
    }
}
