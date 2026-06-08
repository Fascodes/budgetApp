package dev.fascodes.budgetApp.account.service;

import dev.fascodes.budgetApp.account.dto.*;
import dev.fascodes.budgetApp.account.exception.AccountHasTransactionsException;
import dev.fascodes.budgetApp.account.exception.AccountNotFoundException;
import dev.fascodes.budgetApp.account.mapper.AccountMapper;
import dev.fascodes.budgetApp.account.model.Account;
import dev.fascodes.budgetApp.account.repository.AccountRepository;
import dev.fascodes.budgetApp.transaction.model.TransactionType;
import dev.fascodes.budgetApp.transaction.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class AccountService {
    private final AccountMapper accountMapper;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public AccountService(AccountMapper accountMapper, AccountRepository accountRepository,
                          TransactionRepository transactionRepository) {
        this.accountMapper = accountMapper;
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    public List<AccountSummaryResponse> getAllAccounts() {
        return accountRepository.findAll().stream()
                .map(accountMapper::toAccountSummaryResponse)
                .toList();
    }

    public AccountDetailResponse getAccountById(Long id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException(id));
        return accountMapper.toAccountDetailResponse(account);
    }

    public AccountDetailResponse addAccount(AccountRequest request) {
        Account saved = accountRepository.save(accountMapper.toAccount(request));
        return accountMapper.toAccountDetailResponse(saved);
    }

    public void deleteAccount(Long id) {
        if (!accountRepository.existsById(id)) {
            throw new AccountNotFoundException(id);
        }
        if (transactionRepository.existsByAccountId(id)) {
            throw new AccountHasTransactionsException(id);
        }
        accountRepository.deleteById(id);
    }

    public SummaryResponse getAccountSummary(Long id) {
        if (!accountRepository.existsById(id)) {
            throw new AccountNotFoundException(id);
        }
        BigDecimal totalIncome = transactionRepository.sumAmountByAccountAndType(id, TransactionType.INCOME);
        BigDecimal totalExpenses = transactionRepository.sumAmountByAccountAndType(id, TransactionType.EXPENSE);
        List<CategoryExpense> expensesByCategory = transactionRepository
                .sumByCategory(id, TransactionType.EXPENSE).stream()
                .map(p -> new CategoryExpense(p.getCategoryName(), p.getTotal()))
                .toList();
        return new SummaryResponse(totalIncome, totalExpenses, expensesByCategory);
    }
}
