package dev.fascodes.budgetApp.account.service;

import dev.fascodes.budgetApp.account.dto.SummaryResponse;
import dev.fascodes.budgetApp.account.exception.AccountHasTransactionsException;
import dev.fascodes.budgetApp.account.exception.AccountNotFoundException;
import dev.fascodes.budgetApp.account.mapper.AccountMapper;
import dev.fascodes.budgetApp.account.repository.AccountRepository;
import dev.fascodes.budgetApp.transaction.model.TransactionType;
import dev.fascodes.budgetApp.transaction.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock private AccountRepository accountRepository;
    @Mock private TransactionRepository transactionRepository;
    @Mock private AccountMapper accountMapper;

    @InjectMocks private AccountService accountService;

    @Test
    void deleteAccount_accountNotFound_throwsNotFoundException() {
        when(accountRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> accountService.deleteAccount(99L))
                .isInstanceOf(AccountNotFoundException.class);

        verify(accountRepository, never()).deleteById(any());
    }

    @Test
    void deleteAccount_hasTransactions_throwsConflictException() {
        when(accountRepository.existsById(1L)).thenReturn(true);
        when(transactionRepository.existsByAccountId(1L)).thenReturn(true);

        assertThatThrownBy(() -> accountService.deleteAccount(1L))
                .isInstanceOf(AccountHasTransactionsException.class);

        verify(accountRepository, never()).deleteById(any());
    }

    @Test
    void deleteAccount_noTransactions_deletesSuccessfully() {
        when(accountRepository.existsById(1L)).thenReturn(true);
        when(transactionRepository.existsByAccountId(1L)).thenReturn(false);

        accountService.deleteAccount(1L);

        verify(accountRepository).deleteById(1L);
    }

    @Test
    void getAccountSummary_accountNotFound_throwsNotFoundException() {
        when(accountRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> accountService.getAccountSummary(99L))
                .isInstanceOf(AccountNotFoundException.class);
    }

    @Test
    void getAccountSummary_returnsCorrectTotals() {
        when(accountRepository.existsById(1L)).thenReturn(true);
        when(transactionRepository.sumAmountByAccountAndType(1L, TransactionType.INCOME))
                .thenReturn(new BigDecimal("3000.00"));
        when(transactionRepository.sumAmountByAccountAndType(1L, TransactionType.EXPENSE))
                .thenReturn(new BigDecimal("1200.00"));
        when(transactionRepository.sumByCategory(1L, TransactionType.EXPENSE))
                .thenReturn(List.of());

        SummaryResponse summary = accountService.getAccountSummary(1L);

        assertThat(summary.getTotalIncome()).isEqualByComparingTo(new BigDecimal("3000.00"));
        assertThat(summary.getTotalExpenses()).isEqualByComparingTo(new BigDecimal("1200.00"));
        assertThat(summary.getExpensesByCategory()).isEmpty();
    }
}
