package dev.fascodes.budgetApp.account.controller;

import dev.fascodes.budgetApp.account.dto.AccountDetailResponse;
import dev.fascodes.budgetApp.account.dto.AccountRequest;
import dev.fascodes.budgetApp.account.dto.AccountSummaryResponse;
import dev.fascodes.budgetApp.account.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/account")
public class AccountController {
    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping("/all")
    public ResponseEntity<List<AccountSummaryResponse>> getAllAccounts() {
        return ResponseEntity.ok(accountService.getAllAccounts());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AccountDetailResponse> getAccountDetails(@PathVariable Long id) {
        return ResponseEntity.ok(accountService.getAccountById(id));
    }

    @PostMapping
    public ResponseEntity<AccountDetailResponse> addAccount(@Valid @RequestBody AccountRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(accountService.addAccount(request));
    }
}
