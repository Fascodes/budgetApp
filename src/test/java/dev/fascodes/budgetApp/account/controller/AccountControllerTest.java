package dev.fascodes.budgetApp.account.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.fascodes.budgetApp.account.dto.AccountDetailResponse;
import dev.fascodes.budgetApp.account.exception.AccountHasTransactionsException;
import dev.fascodes.budgetApp.account.exception.AccountNotFoundException;
import dev.fascodes.budgetApp.account.service.AccountService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AccountController.class)
class AccountControllerTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean AccountService accountService;

    @Test
    void addAccount_blankName_returns400WithMessage() throws Exception {
        String body = """
                {"name": ""}
                """;

        mockMvc.perform(post("/api/account")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.name").value("Account name is required"));
    }

    @Test
    void addAccount_nullName_returns400() throws Exception {
        String body = """
                {}
                """;

        mockMvc.perform(post("/api/account")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.name").exists());
    }

    @Test
    void addAccount_negativeBalance_returns400WithMessage() throws Exception {
        String body = """
                {"name": "Konto", "balance": -100}
                """;

        mockMvc.perform(post("/api/account")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.balance").value("Balance cannot be negative"));
    }

    @Test
    void addAccount_valid_returns201() throws Exception {
        AccountDetailResponse response = new AccountDetailResponse();
        response.setId(1L);
        response.setName("Konto główne");
        response.setBalance(BigDecimal.ZERO);

        when(accountService.addAccount(any())).thenReturn(response);

        String body = """
                {"name": "Konto główne"}
                """;

        mockMvc.perform(post("/api/account")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Konto główne"));
    }

    @Test
    void getAccountDetails_notFound_returns404WithMessage() throws Exception {
        when(accountService.getAccountById(99L))
                .thenThrow(new AccountNotFoundException(99L));

        mockMvc.perform(get("/api/account/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Account not found with id: 99"));
    }

    @Test
    void deleteAccount_hasTransactions_returns409WithMessage() throws Exception {
        doThrow(new AccountHasTransactionsException(1L))
                .when(accountService).deleteAccount(1L);

        mockMvc.perform(delete("/api/account/1"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void deleteAccount_notFound_returns404() throws Exception {
        doThrow(new AccountNotFoundException(99L))
                .when(accountService).deleteAccount(99L);

        mockMvc.perform(delete("/api/account/99"))
                .andExpect(status().isNotFound());
    }
}
