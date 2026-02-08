package com.example.moneytransfer.service;

import com.example.moneytransfer.domain.Account;
import com.example.moneytransfer.domain.TransactionLog;
import com.example.moneytransfer.dto.AccountResponse;
import com.example.moneytransfer.exception.AccountNotFoundException;
import com.example.moneytransfer.repository.AccountRepository;
import com.example.moneytransfer.repository.TransactionLogRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final TransactionLogRepository transactionLogRepository;

    public AccountService(AccountRepository accountRepository,
            TransactionLogRepository transactionLogRepository) {
        this.accountRepository = accountRepository;
        this.transactionLogRepository = transactionLogRepository;
    }

    public Account getAccount(Long id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException(id));
    }

    public BigDecimal getBalance(Long id) {
        Account account = getAccount(id);
        return account.getBalance();
    }

    public List<TransactionLog> getTransactions(Long id) {
        return transactionLogRepository.findByFromAccountIdOrToAccountIdOrderByCreatedOnDesc(id, id);
    }
}
