package com.example.moneytransfer.service;

import com.example.moneytransfer.domain.Account;
import com.example.moneytransfer.domain.TransactionLog;
import com.example.moneytransfer.dto.TransferRequest;
import com.example.moneytransfer.dto.TransferResponse;
import com.example.moneytransfer.enums.TransactionStatus;
import com.example.moneytransfer.exception.AccountNotActiveException;
import com.example.moneytransfer.exception.AccountNotFoundException;
import com.example.moneytransfer.exception.DuplicateTransferException;
import com.example.moneytransfer.exception.InsufficientBalanceException;
import com.example.moneytransfer.repository.AccountRepository;
import com.example.moneytransfer.repository.TransactionLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class TransferService {

    private final AccountRepository accountRepository;
    private final TransactionLogRepository transactionLogRepository;

    public TransferService(AccountRepository accountRepository,
            TransactionLogRepository transactionLogRepository) {
        this.accountRepository = accountRepository;
        this.transactionLogRepository = transactionLogRepository;
    }

    @Transactional
    public TransferResponse transfer(TransferRequest request) {
        checkOwnership(request.fromAccountId());
        validateTransfer(request);

        Optional<TransactionLog> existing = transactionLogRepository.findByIdempotencyKey(request.idempotencyKey());
        if (existing.isPresent()) {
            throw new DuplicateTransferException(request.idempotencyKey());
        }

        return executeTransfer(request);
    }

    private void checkOwnership(Long accountId) {
        String authenticatedId = org.springframework.security.core.context.SecurityContextHolder.getContext()
                .getAuthentication().getName();
        if (!authenticatedId.equals(String.valueOf(accountId))) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "You are not authorized to transfer from this account.");
        }
    }

    public void validateTransfer(TransferRequest request) {
        Account from = accountRepository.findById(request.fromAccountId())
                .orElseThrow(() -> new AccountNotFoundException(request.fromAccountId()));
        Account to = accountRepository.findById(request.toAccountId())
                .orElseThrow(() -> new AccountNotFoundException(request.toAccountId()));

        if (!from.isActive()) {
            throw new AccountNotActiveException(from.getId(), from.getStatus());
        }
        if (!to.isActive()) {
            throw new AccountNotActiveException(to.getId(), to.getStatus());
        }
        if (from.getBalance().compareTo(request.amount()) < 0) {
            throw new InsufficientBalanceException(from.getId(), from.getBalance(), request.amount());
        }
    }

    @Transactional
    public TransferResponse executeTransfer(TransferRequest request) {
        Account from = accountRepository.findById(request.fromAccountId())
                .orElseThrow(() -> new AccountNotFoundException(request.fromAccountId()));
        Account to = accountRepository.findById(request.toAccountId())
                .orElseThrow(() -> new AccountNotFoundException(request.toAccountId()));

        try {
            from.debit(request.amount());
            to.credit(request.amount());
            accountRepository.save(from);
            accountRepository.save(to);

            TransactionLog log = TransactionLog.success(
                    request.fromAccountId(),
                    request.toAccountId(),
                    request.amount(),
                    request.idempotencyKey());
            log = transactionLogRepository.save(log);

            return TransferResponse.success(
                    log.getId(),
                    request.fromAccountId(),
                    request.toAccountId(),
                    request.amount());
        } catch (AccountNotActiveException | InsufficientBalanceException e) {
            TransactionLog failedLog = TransactionLog.failed(
                    request.fromAccountId(),
                    request.toAccountId(),
                    request.amount(),
                    request.idempotencyKey(),
                    e.getMessage());
            transactionLogRepository.save(failedLog);
            return TransferResponse.failed(
                    failedLog.getId(),
                    e.getMessage(),
                    request.fromAccountId(),
                    request.toAccountId(),
                    request.amount());
        }
    }
}
