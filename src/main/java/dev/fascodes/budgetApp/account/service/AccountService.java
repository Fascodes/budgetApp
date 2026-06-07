package dev.fascodes.budgetApp.account.service;


import dev.fascodes.budgetApp.account.dto.AccountSummaryResponse;
import dev.fascodes.budgetApp.account.mapper.AccountMapper;
import dev.fascodes.budgetApp.account.repository.AccountRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AccountService {
    private AccountMapper accountMapper;
    private AccountRepository accountRepository;

    public AccountService(AccountMapper accountMapper, AccountRepository accountRepository) {
        this.accountMapper = accountMapper;
        this.accountRepository = accountRepository;
    }

    public List<AccountSummaryResponse> getAllAccounts(){
        return accountRepository.findAll().stream()
                .map(accountMapper::toAccountSummaryResponse)
                .toList();
    }
}
