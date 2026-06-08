package dev.fascodes.budgetApp.transaction.exception;

import dev.fascodes.budgetApp.common.ResourceNotFoundException;

public class TransactionNotFoundException extends ResourceNotFoundException {
    public TransactionNotFoundException(Long id) {
        super("Transaction not found with id: " + id);
    }
}
