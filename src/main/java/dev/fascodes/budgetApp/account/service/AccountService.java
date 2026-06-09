package dev.fascodes.budgetApp.account.service;

import dev.fascodes.budgetApp.account.dto.*;
import dev.fascodes.budgetApp.account.exception.AccountHasTransactionsException;
import dev.fascodes.budgetApp.account.exception.AccountNotFoundException;
import dev.fascodes.budgetApp.account.mapper.AccountMapper;
import dev.fascodes.budgetApp.account.model.Account;
import dev.fascodes.budgetApp.account.repository.AccountRepository;
import dev.fascodes.budgetApp.transaction.model.Transaction;
import dev.fascodes.budgetApp.transaction.model.TransactionType;
import dev.fascodes.budgetApp.transaction.repository.TransactionRepository;
import dev.fascodes.budgetApp.transaction.repository.TransactionSpecification;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
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

    public byte[] exportTransactionsToCsv(Long accountId) {
        if (!accountRepository.existsById(accountId)) {
            throw new AccountNotFoundException(accountId);
        }
        Specification<Transaction> spec = Specification.where(TransactionSpecification.hasAccount(accountId));
        List<Transaction> transactions = transactionRepository.findAll(spec);

        StringBuilder csv = new StringBuilder("id,date,type,category,amount,description\n");
        for (Transaction t : transactions) {
            csv.append(t.getId()).append(',')
               .append(t.getDate()).append(',')
               .append(t.getType()).append(',')
               .append(escapeCsv(t.getCategory().getName())).append(',')
               .append(t.getAmount()).append(',')
               .append(escapeCsv(t.getDescription() != null ? t.getDescription() : ""))
               .append('\n');
        }
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    private String escapeCsv(String value) {
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
