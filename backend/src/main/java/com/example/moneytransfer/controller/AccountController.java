package com.example.moneytransfer.controller;

import com.example.moneytransfer.dto.AccountResponse;
import com.example.moneytransfer.dto.TransactionLogResponse;
import com.example.moneytransfer.service.AccountService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping
    public ResponseEntity<List<AccountResponse>> getMyAccounts(java.security.Principal principal) {
        return ResponseEntity.ok(accountService.getAccountsByUser(principal.getName()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AccountResponse> getAccount(@PathVariable Long id, java.security.Principal principal) {
        return ResponseEntity.ok(accountService.getAccount(id, principal.getName()));
    }

    @GetMapping("/{id}/lookup")
    public ResponseEntity<AccountResponse> lookupAccount(@PathVariable Long id) {
        return ResponseEntity.ok(accountService.lookupAccount(id));
    }

    @GetMapping("/{id}/balance")
    public ResponseEntity<BigDecimal> getBalance(@PathVariable Long id, java.security.Principal principal) {
        return ResponseEntity.ok(accountService.getBalance(id, principal.getName()));
    }

    @GetMapping("/{id}/transactions")
    public ResponseEntity<List<TransactionLogResponse>> getTransactions(@PathVariable Long id, java.security.Principal principal) {
        return ResponseEntity.ok(accountService.getTransactions(id, principal.getName()));
    }
}

