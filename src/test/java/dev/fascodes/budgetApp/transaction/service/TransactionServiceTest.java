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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock private TransactionRepository transactionRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private AccountRepository accountRepository;
    @Mock private TransactionMapper transactionMapper;

    @InjectMocks private TransactionService transactionService;

    @Test
    void addTransaction_income_increasesBalance() {
        TransactionRequest request = buildRequest(TransactionType.INCOME, new BigDecimal("500.00"));
        Account account = accountWithBalance(new BigDecimal("1000.00"));
        Category category = new Category();
        Transaction transaction = transactionStub(account, category, TransactionType.INCOME, new BigDecimal("500.00"));

        when(accountRepository.findByIdWithLock(1L)).thenReturn(Optional.of(account));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(transactionMapper.toTransaction(request, account, category)).thenReturn(transaction);
        when(transactionRepository.save(transaction)).thenReturn(transaction);
        when(transactionMapper.toTransactionResponse(transaction)).thenReturn(new TransactionResponse());

        transactionService.addTransaction(request);

        assertThat(account.getBalance()).isEqualByComparingTo(new BigDecimal("1500.00"));
        verify(accountRepository).save(account);
    }

    @Test
    void addTransaction_expense_decreasesBalance() {
        TransactionRequest request = buildRequest(TransactionType.EXPENSE, new BigDecimal("200.00"));
        Account account = accountWithBalance(new BigDecimal("1000.00"));
        Category category = new Category();
        Transaction transaction = transactionStub(account, category, TransactionType.EXPENSE, new BigDecimal("200.00"));

        when(accountRepository.findByIdWithLock(1L)).thenReturn(Optional.of(account));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(transactionMapper.toTransaction(request, account, category)).thenReturn(transaction);
        when(transactionRepository.save(transaction)).thenReturn(transaction);
        when(transactionMapper.toTransactionResponse(transaction)).thenReturn(new TransactionResponse());

        transactionService.addTransaction(request);

        assertThat(account.getBalance()).isEqualByComparingTo(new BigDecimal("800.00"));
        verify(accountRepository).save(account);
    }

    @Test
    void addTransaction_accountNotFound_throwsException() {
        TransactionRequest request = buildRequest(TransactionType.INCOME, new BigDecimal("100.00"));
        when(accountRepository.findByIdWithLock(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.addTransaction(request))
                .isInstanceOf(AccountNotFoundException.class);
    }

    @Test
    void addTransaction_categoryNotFound_throwsException() {
        TransactionRequest request = buildRequest(TransactionType.INCOME, new BigDecimal("100.00"));
        Account account = accountWithBalance(new BigDecimal("1000.00"));

        when(accountRepository.findByIdWithLock(1L)).thenReturn(Optional.of(account));
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.addTransaction(request))
                .isInstanceOf(CategoryNotFoundException.class);
    }

    @Test
    void deleteTransaction_income_revertsBalance() {
        Account account = accountWithBalance(new BigDecimal("1500.00"));
        Transaction transaction = transactionStub(account, new Category(), TransactionType.INCOME, new BigDecimal("500.00"));
        transaction.setId(1L);

        when(transactionRepository.findById(1L)).thenReturn(Optional.of(transaction));
        when(accountRepository.findByIdWithLock(1L)).thenReturn(Optional.of(account));

        transactionService.deleteTransaction(1L);

        assertThat(account.getBalance()).isEqualByComparingTo(new BigDecimal("1000.00"));
        verify(transactionRepository).delete(transaction);
    }

    @Test
    void deleteTransaction_expense_revertsBalance() {
        Account account = accountWithBalance(new BigDecimal("800.00"));
        Transaction transaction = transactionStub(account, new Category(), TransactionType.EXPENSE, new BigDecimal("200.00"));
        transaction.setId(1L);

        when(transactionRepository.findById(1L)).thenReturn(Optional.of(transaction));
        when(accountRepository.findByIdWithLock(1L)).thenReturn(Optional.of(account));

        transactionService.deleteTransaction(1L);

        assertThat(account.getBalance()).isEqualByComparingTo(new BigDecimal("1000.00"));
        verify(transactionRepository).delete(transaction);
    }

    @Test
    void deleteTransaction_notFound_throwsException() {
        when(transactionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.deleteTransaction(99L))
                .isInstanceOf(TransactionNotFoundException.class);
    }

    // --- helpers ---

    private TransactionRequest buildRequest(TransactionType type, BigDecimal amount) {
        TransactionRequest request = new TransactionRequest();
        request.setAccountId(1L);
        request.setCategoryId(1L);
        request.setType(type);
        request.setAmount(amount);
        request.setDate(LocalDateTime.now());
        return request;
    }

    private Account accountWithBalance(BigDecimal balance) {
        Account account = new Account();
        account.setId(1L);
        account.setBalance(balance);
        return account;
    }

    private Transaction transactionStub(Account account, Category category, TransactionType type, BigDecimal amount) {
        Transaction t = new Transaction();
        t.setAccount(account);
        t.setCategory(category);
        t.setType(type);
        t.setAmount(amount);
        return t;
    }
}
