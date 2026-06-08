package dev.fascodes.budgetApp.account.service;

import dev.fascodes.budgetApp.account.dto.AccountDetailResponse;
import dev.fascodes.budgetApp.account.dto.AccountRequest;
import dev.fascodes.budgetApp.account.dto.AccountSummaryResponse;
import dev.fascodes.budgetApp.account.exception.AccountNotFoundException;
import dev.fascodes.budgetApp.account.mapper.AccountMapper;
import dev.fascodes.budgetApp.account.model.Account;
import dev.fascodes.budgetApp.account.repository.AccountRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AccountService {
    private final AccountMapper accountMapper;
    private final AccountRepository accountRepository;

    public AccountService(AccountMapper accountMapper, AccountRepository accountRepository) {
        this.accountMapper = accountMapper;
        this.accountRepository = accountRepository;
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
}
