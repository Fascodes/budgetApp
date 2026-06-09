package dev.fascodes.budgetApp.transaction.controller;

import dev.fascodes.budgetApp.transaction.exception.TransactionNotFoundException;
import dev.fascodes.budgetApp.transaction.service.TransactionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TransactionController.class)
class TransactionControllerTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean TransactionService transactionService;

    @Test
    void addTransaction_missingAccountId_returns400WithMessage() throws Exception {
        String body = """
                {
                    "categoryId": 1,
                    "type": "INCOME",
                    "amount": 100.00,
                    "date": "2024-03-01T10:00:00"
                }
                """;

        mockMvc.perform(post("/api/transaction")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.accountId").value("Account id is required"));
    }

    @Test
    void addTransaction_zeroAmount_returns400WithMessage() throws Exception {
        String body = """
                {
                    "accountId": 1,
                    "categoryId": 1,
                    "type": "EXPENSE",
                    "amount": 0,
                    "date": "2024-03-01T10:00:00"
                }
                """;

        mockMvc.perform(post("/api/transaction")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.amount").value("Amount must be greater than zero"));
    }

    @Test
    void addTransaction_missingType_returns400WithMessage() throws Exception {
        String body = """
                {
                    "accountId": 1,
                    "categoryId": 1,
                    "amount": 100.00,
                    "date": "2024-03-01T10:00:00"
                }
                """;

        mockMvc.perform(post("/api/transaction")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value("Transaction type is required"));
    }

    @Test
    void addTransaction_missingDate_returns400WithMessage() throws Exception {
        String body = """
                {
                    "accountId": 1,
                    "categoryId": 1,
                    "type": "INCOME",
                    "amount": 100.00
                }
                """;

        mockMvc.perform(post("/api/transaction")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.date").value("Date is required"));
    }

    @Test
    void deleteTransaction_notFound_returns404WithMessage() throws Exception {
        doThrow(new TransactionNotFoundException(99L))
                .when(transactionService).deleteTransaction(99L);

        mockMvc.perform(delete("/api/transaction/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Transaction not found with id: 99"));
    }
}
