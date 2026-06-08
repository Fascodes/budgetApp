package dev.fascodes.budgetApp.account.exception;

public class AccountHasTransactionsException extends RuntimeException {
    public AccountHasTransactionsException(Long id) {
        super("Cannot delete account " + id + " because it has existing transactions");
    }
}
