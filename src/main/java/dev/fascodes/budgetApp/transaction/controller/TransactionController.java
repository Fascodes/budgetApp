package dev.fascodes.budgetApp.transaction.controller;

import dev.fascodes.budgetApp.transaction.dto.TransactionRequest;
import dev.fascodes.budgetApp.transaction.dto.TransactionResponse;
import dev.fascodes.budgetApp.transaction.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import java.time.LocalDate;

@Tag(name = "Transactions", description = "Managing income and expense transactions")
@RestController
@RequestMapping("/api/transaction")
public class TransactionController {
    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Operation(
            summary = "Get transactions",
            description = "Returns all transactions for the given account. Results can be narrowed by date range and/or category name."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List returned successfully"),
            @ApiResponse(responseCode = "404", description = "Account not found")
    })
    @GetMapping
    public ResponseEntity<List<TransactionResponse>> getTransactions(
            @Parameter(description = "ID of the account", required = true) @RequestParam Long accountId,
            @Parameter(description = "Start date (inclusive), format: yyyy-MM-dd") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @Parameter(description = "End date (inclusive), format: yyyy-MM-dd") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @Parameter(description = "Filter by category name (case-insensitive)") @RequestParam(required = false) String category) {
        return ResponseEntity.ok(transactionService.getTransactions(accountId, from, to, category));
    }

    @Operation(summary = "Add transaction", description = "Creates a new transaction and automatically updates the account balance.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Transaction created"),
            @ApiResponse(responseCode = "400", description = "Validation error — missing or invalid fields"),
            @ApiResponse(responseCode = "404", description = "Account or category not found")
    })
    @PostMapping
    public ResponseEntity<TransactionResponse> addTransaction(@Valid @RequestBody TransactionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(transactionService.addTransaction(request));
    }

    @Operation(summary = "Delete transaction", description = "Deletes a transaction and reverts its effect on the account balance.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Transaction deleted"),
            @ApiResponse(responseCode = "404", description = "Transaction not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransaction(@PathVariable Long id) {
        transactionService.deleteTransaction(id);
        return ResponseEntity.noContent().build();
    }
}
