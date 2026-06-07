package dev.fascodes.budgetApp.account.controller;


import dev.fascodes.budgetApp.account.dto.AccountSummaryResponse;
import dev.fascodes.budgetApp.account.service.AccountService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/account")
public class AccountController {
    private AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping("/all")
    public ResponseEntity<List<AccountSummaryResponse>> getAllAccounts(){
        return ResponseEntity.ok(accountService.getAllAccounts());
    }
}
