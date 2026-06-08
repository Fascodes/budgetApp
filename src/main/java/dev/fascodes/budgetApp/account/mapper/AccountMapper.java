package dev.fascodes.budgetApp.account.mapper;

import dev.fascodes.budgetApp.account.dto.AccountDetailResponse;
import dev.fascodes.budgetApp.account.dto.AccountRequest;
import dev.fascodes.budgetApp.account.dto.AccountSummaryResponse;
import dev.fascodes.budgetApp.account.model.Account;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
public class AccountMapper {

    public AccountSummaryResponse toAccountSummaryResponse(Account account) {
        AccountSummaryResponse response = new AccountSummaryResponse();
        response.setId(account.getId());
        response.setName(account.getName());
        return response;
    }

    public AccountDetailResponse toAccountDetailResponse(Account account) {
        AccountDetailResponse response = new AccountDetailResponse();
        response.setId(account.getId());
        response.setName(account.getName());
        response.setBalance(account.getBalance());
        response.setCreatedAt(account.getCreatedAt());
        return response;
    }

    public Account toAccount(AccountRequest request) {
        Account account = new Account();
        account.setName(request.getName());
        account.setBalance(request.getBalance() != null ? request.getBalance() : BigDecimal.ZERO);
        account.setCreatedAt(LocalDateTime.now());
        return account;
    }
}
