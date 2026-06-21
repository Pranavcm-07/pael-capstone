package com.example.moneytransfer.service;

import com.example.moneytransfer.domain.Account;
import com.example.moneytransfer.domain.entity.TransactionLog;
import com.example.moneytransfer.dto.AccountResponse;
import com.example.moneytransfer.dto.TransactionLogResponse;
import com.example.moneytransfer.exception.AccountNotFoundException;
import com.example.moneytransfer.repository.AccountRepository;
import com.example.moneytransfer.repository.TransactionLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final TransactionLogRepository transactionLogRepository;
    private final com.example.moneytransfer.repository.AccountStatusAuditRepository accountStatusAuditRepository;

    public AccountService(AccountRepository accountRepository,
                          TransactionLogRepository transactionLogRepository,
                          com.example.moneytransfer.repository.AccountStatusAuditRepository accountStatusAuditRepository) {
        this.accountRepository = accountRepository;
        this.transactionLogRepository = transactionLogRepository;
        this.accountStatusAuditRepository = accountStatusAuditRepository;
    }

    @Transactional(readOnly = true)
    public AccountResponse getAccount(Long id, String username) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException(id));
        
        if (!account.getUser().getUsername().equals(username)) {
            throw new com.example.moneytransfer.exception.UnauthorizedAccessException("Unauthorized access to account");
        }
        
        return toResponse(account);
    }

    @Transactional(readOnly = true)
    public AccountResponse lookupAccount(Long id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException(id));
        
        AccountResponse response = new AccountResponse();
        response.setId(account.getId());
        response.setHolderName(account.getHolderName());
        response.setStatus(account.getStatus().name());
        // Balance is purposely left as null for security
        return response;
    }

    @Transactional(readOnly = true)
    public List<AccountResponse> getAccountsByUser(String username) {
        return accountRepository.findByUserUsername(username).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public BigDecimal getBalance(Long id, String username) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException(id));

        if (!account.getUser().getUsername().equals(username)) {
            throw new com.example.moneytransfer.exception.UnauthorizedAccessException("Unauthorized access to account");
        }
        return account.getBalance();
    }


    @Transactional(readOnly = true)
    public List<TransactionLogResponse> getTransactions(Long id, String username) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException(id));

        if (!account.getUser().getUsername().equals(username)) {
            throw new com.example.moneytransfer.exception.UnauthorizedAccessException("Unauthorized access to account");
        }
        
        List<TransactionLog> logs = transactionLogRepository.findByFromAccountIdOrToAccountId(id, id);
        
        // Get all account IDs from the transactions
        List<Long> accountIds = logs.stream()
                .flatMap(log -> List.of(log.getFromAccountId(), log.getToAccountId()).stream())
                .distinct()
                .toList();
        
        // Fetch all accounts and map by ID for quick lookup
        Map<Long, Account> accountMap = accountRepository.findAllById(accountIds).stream()
                .collect(Collectors.toMap(Account::getId, acc -> acc));
        
        // Map to response DTOs
        return logs.stream()
                .map(log -> toTransactionLogResponse(log, accountMap))
                .toList();
    }

    @Transactional
    public void updateAccountStatus(Long id, com.example.moneytransfer.domain.enums.AccountStatus newStatus, String reason, String changedBy) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException(id));

        com.example.moneytransfer.domain.enums.AccountStatus oldStatus = account.getStatus();
        account.setStatus(newStatus);
        accountRepository.save(account);

        com.example.moneytransfer.domain.entity.AccountStatusAudit audit = new com.example.moneytransfer.domain.entity.AccountStatusAudit(
                id, oldStatus, newStatus, reason, changedBy
        );
        accountStatusAuditRepository.save(audit);
    }

    private TransactionLogResponse toTransactionLogResponse(TransactionLog log, Map<Long, Account> accountMap) {
        TransactionLogResponse response = new TransactionLogResponse();
        response.setId(log.getId());
        response.setFromAccountId(log.getFromAccountId());
        response.setToAccountId(log.getToAccountId());
        response.setAmount(log.getAmount());
        response.setStatus(log.getStatus());
        response.setFailureReason(log.getFailureReason());
        response.setIdempotencyKey(log.getIdempotencyKey());
        response.setCreatedOn(log.getCreatedOn());
        
        // Set account holder names
        Account fromAccount = accountMap.get(log.getFromAccountId());
        if (fromAccount != null) {
            response.setFromAccountHolderName(fromAccount.getHolderName());
        }
        
        Account toAccount = accountMap.get(log.getToAccountId());
        if (toAccount != null) {
            response.setToAccountHolderName(toAccount.getHolderName());
        }
        
        return response;
    }

    private AccountResponse toResponse(Account account) {
        AccountResponse response = new AccountResponse();
        response.setId(account.getId());
        response.setHolderName(account.getHolderName());
        response.setBalance(account.getBalance());
        response.setStatus(account.getStatus().name());
        return response;
    }
}

