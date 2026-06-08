package dev.fascodes.budgetApp.transaction.mapper;

import dev.fascodes.budgetApp.account.model.Account;
import dev.fascodes.budgetApp.transaction.dto.TransactionRequest;
import dev.fascodes.budgetApp.transaction.dto.TransactionResponse;
import dev.fascodes.budgetApp.transaction.model.Category;
import dev.fascodes.budgetApp.transaction.model.Transaction;
import org.springframework.stereotype.Component;

@Component
public class TransactionMapper {

    public Transaction toTransaction(TransactionRequest request, Account account, Category category) {
        Transaction transaction = new Transaction();
        transaction.setAccount(account);
        transaction.setCategory(category);
        transaction.setType(request.getType());
        transaction.setAmount(request.getAmount());
        transaction.setDescription(request.getDescription());
        transaction.setDate(request.getDate());
        return transaction;
    }

    public TransactionResponse toTransactionResponse(Transaction transaction) {
        TransactionResponse response = new TransactionResponse();
        response.setId(transaction.getId());
        response.setAccountId(transaction.getAccount().getId());
        response.setCategoryId(transaction.getCategory().getId());
        response.setCategoryName(transaction.getCategory().getName());
        response.setType(transaction.getType());
        response.setAmount(transaction.getAmount());
        response.setDescription(transaction.getDescription());
        response.setDate(transaction.getDate());
        return response;
    }
}
