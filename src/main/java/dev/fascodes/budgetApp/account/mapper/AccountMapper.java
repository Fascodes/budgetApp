package dev.fascodes.budgetApp.account.mapper;

import dev.fascodes.budgetApp.account.dto.AccountSummaryResponse;
import dev.fascodes.budgetApp.account.model.Account;
import org.springframework.stereotype.Component;

@Component
public class AccountMapper {
    public AccountSummaryResponse toAccountSummaryResponse(Account account){
        AccountSummaryResponse response = new AccountSummaryResponse();
        response.setId(account.getId());
        response.setName(account.getName());
        return response;
    }
}
