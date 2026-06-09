package dev.fascodes.budgetApp.account.controller;

import dev.fascodes.budgetApp.account.dto.*;
import dev.fascodes.budgetApp.account.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Accounts", description = "Managing bank/budget accounts")
@RestController
@RequestMapping("/api/account")
public class AccountController {
    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @Operation(summary = "Get all accounts", description = "Returns a list of all accounts with basic info (id, name, balance).")
    @ApiResponse(responseCode = "200", description = "List returned successfully")
    @GetMapping("/all")
    public ResponseEntity<List<AccountSummaryResponse>> getAllAccounts() {
        return ResponseEntity.ok(accountService.getAllAccounts());
    }

    @Operation(summary = "Get account details", description = "Returns full details of a single account including creation date.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Account found"),
            @ApiResponse(responseCode = "404", description = "Account not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<AccountDetailResponse> getAccountDetails(@PathVariable Long id) {
        return ResponseEntity.ok(accountService.getAccountById(id));
    }

    @Operation(summary = "Create account", description = "Creates a new account. Initial balance defaults to 0 if not provided.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Account created"),
            @ApiResponse(responseCode = "400", description = "Validation error — name is blank or balance is negative")
    })
    @PostMapping
    public ResponseEntity<AccountDetailResponse> addAccount(@Valid @RequestBody AccountRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(accountService.addAccount(request));
    }

    @Operation(summary = "Delete account", description = "Deletes an account. Fails with 409 if the account has any transactions.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Account deleted"),
            @ApiResponse(responseCode = "404", description = "Account not found"),
            @ApiResponse(responseCode = "409", description = "Account has existing transactions and cannot be deleted")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAccount(@PathVariable Long id) {
        accountService.deleteAccount(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get account summary", description = "Returns total income, total expenses and a breakdown of expenses by category for the given account.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Summary returned successfully"),
            @ApiResponse(responseCode = "404", description = "Account not found")
    })
    @GetMapping("/{id}/summary")
    public ResponseEntity<SummaryResponse> getAccountSummary(@PathVariable Long id) {
        return ResponseEntity.ok(accountService.getAccountSummary(id));
    }
}
