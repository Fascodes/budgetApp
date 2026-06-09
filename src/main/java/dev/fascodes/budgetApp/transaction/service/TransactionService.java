package dev.fascodes.budgetApp.transaction.service;

import dev.fascodes.budgetApp.account.exception.AccountNotFoundException;
import dev.fascodes.budgetApp.account.model.Account;
import dev.fascodes.budgetApp.account.repository.AccountRepository;
import dev.fascodes.budgetApp.transaction.dto.TransactionRequest;
import dev.fascodes.budgetApp.transaction.dto.TransactionResponse;
import dev.fascodes.budgetApp.transaction.exception.CategoryNotFoundException;
import dev.fascodes.budgetApp.transaction.exception.TransactionNotFoundException;
import dev.fascodes.budgetApp.transaction.mapper.TransactionMapper;
import dev.fascodes.budgetApp.transaction.model.Category;
import dev.fascodes.budgetApp.transaction.model.Transaction;
import dev.fascodes.budgetApp.transaction.model.TransactionType;
import dev.fascodes.budgetApp.transaction.repository.CategoryRepository;
import dev.fascodes.budgetApp.transaction.repository.TransactionRepository;
import dev.fascodes.budgetApp.transaction.repository.TransactionSpecification;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final AccountRepository accountRepository;
    private final TransactionMapper transactionMapper;

    public TransactionService(TransactionRepository transactionRepository,
                              CategoryRepository categoryRepository,
                              AccountRepository accountRepository,
                              TransactionMapper transactionMapper) {
        this.transactionRepository = transactionRepository;
        this.categoryRepository = categoryRepository;
        this.accountRepository = accountRepository;
        this.transactionMapper = transactionMapper;
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> getTransactions(Long accountId, LocalDate from, LocalDate to, String category) {
        Specification<Transaction> spec = Specification.where(TransactionSpecification.hasAccount(accountId));
        if(from != null) spec = spec.and(TransactionSpecification.dateFrom(from));
        if(to != null) spec = spec.and(TransactionSpecification.dateTo(to));
        if(category != null && !category.isBlank()) spec = spec.and(TransactionSpecification.hasCategory(category));
        return transactionRepository.findAll(spec).stream()
                .map(transactionMapper::toTransactionResponse)
                .toList();
    }

    @Transactional
    public TransactionResponse addTransaction(TransactionRequest request) {
        Account account = accountRepository.findByIdWithLock(request.getAccountId())
                .orElseThrow(() -> new AccountNotFoundException(request.getAccountId()));
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new CategoryNotFoundException(request.getCategoryId()));

        Transaction transaction = transactionMapper.toTransaction(request, account, category);
        Transaction saved = transactionRepository.save(transaction);

        applyBalanceChange(account, request.getType(), request.getAmount(), false);
        accountRepository.save(account);

        return transactionMapper.toTransactionResponse(saved);
    }

    @Transactional
    public void deleteTransaction(Long id) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new TransactionNotFoundException(id));

        Long accountId = transaction.getAccount().getId();
        Account account = accountRepository.findByIdWithLock(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));
        applyBalanceChange(account, transaction.getType(), transaction.getAmount(), true);
        accountRepository.save(account);

        transactionRepository.delete(transaction);
    }

    private void applyBalanceChange(Account account, TransactionType type, BigDecimal amount, boolean reverse) {
        boolean isIncome = type == TransactionType.INCOME;
        if (isIncome != reverse) {
            account.setBalance(account.getBalance().add(amount));
        } else {
            account.setBalance(account.getBalance().subtract(amount));
        }
    }
}
