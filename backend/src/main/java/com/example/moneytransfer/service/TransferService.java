package com.example.moneytransfer.service;

import com.example.moneytransfer.domain.Account;
import com.example.moneytransfer.domain.entity.TransactionLog;
import com.example.moneytransfer.domain.enums.TransactionStatus;
import com.example.moneytransfer.dto.TransferRequest;
import com.example.moneytransfer.dto.TransferResponse;
import com.example.moneytransfer.exception.AccountNotFoundException;
import com.example.moneytransfer.repository.AccountRepository;
import com.example.moneytransfer.repository.TransactionLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class TransferService {

    private final AccountRepository accountRepository;
    private final TransactionLogRepository transactionLogRepository;
    private final TransactionLogService transactionLogService;
    private final RewardService rewardService;
    private final SystemSettingService systemSettingService;

    public TransferService(AccountRepository accountRepository,
                           TransactionLogRepository transactionLogRepository,
                           TransactionLogService transactionLogService,
                           RewardService rewardService,
                           SystemSettingService systemSettingService) {
        this.accountRepository = accountRepository;
        this.transactionLogRepository = transactionLogRepository;
        this.transactionLogService = transactionLogService;
        this.rewardService = rewardService;
        this.systemSettingService = systemSettingService;
    }

    @Transactional
    public TransferResponse transfer(TransferRequest request, String username) {

        // 1) Idempotency check (if key exists, return prior result)
        Optional<TransactionLog> existing =
                transactionLogRepository.findByIdempotencyKey(request.getIdempotencyKey());

        if (existing.isPresent()) {
            TransactionLog log = existing.get();

            TransferResponse response = new TransferResponse();
            response.setTransactionId(log.getId().toString());
            response.setStatus(log.getStatus().name());
            response.setMessage(log.getStatus() == TransactionStatus.SUCCESS
                    ? "Transfer already completed (idempotent replay)"
                    : "Transfer already failed (idempotent replay)");
            response.setDebitedFrom(log.getFromAccountId());
            response.setCreditedTo(log.getToAccountId());
            response.setAmount(log.getAmount());
            response.setRewardPointsEarned(rewardService.getPointsForTransaction(log.getId()));
            return response;
        }

        UUID txId = UUID.randomUUID();

        try {
            // 2) Load accounts
            Account source = accountRepository.findById(request.getFromAccountId())
                    .orElseThrow(() -> new AccountNotFoundException(request.getFromAccountId()));

            // Verify ownership
            if (!source.getUser().getUsername().equals(username)) {
                throw new com.example.moneytransfer.exception.UnauthorizedAccessException("Unauthorized: Source account does not belong to user");
            }

            Account destination = accountRepository.findById(request.getToAccountId())
                    .orElseThrow(() -> new AccountNotFoundException(request.getToAccountId()));

            // 3) Validate
            if (!systemSettingService.isTransfersEnabled()) {
                throw new IllegalArgumentException("VAL-422:Transfers are currently disabled by the administrator");
            }

            java.math.BigDecimal amount = request.getAmount();
            java.math.BigDecimal minAmount = systemSettingService.getMinTransferAmount();
            java.math.BigDecimal maxAmount = systemSettingService.getMaxTransferAmount();

            if (amount.compareTo(minAmount) < 0) {
                throw new IllegalArgumentException("VAL-422:Transfer amount is below the minimum allowed limit of $" + minAmount);
            }
            if (amount.compareTo(maxAmount) > 0) {
                throw new IllegalArgumentException("VAL-422:Transfer amount exceeds the maximum allowed limit of $" + maxAmount);
            }

            if (source.getId().equals(destination.getId())) {
                throw new com.example.moneytransfer.exception.SelfTransferException("Source and destination accounts must be different");
            }

            if (isAdminAccount(destination)) {
                throw new IllegalArgumentException("VAL-422:Cannot transfer money to an administrator account");
            }

            // 4) Execute transfer
            source.debit(request.getAmount());
            destination.credit(request.getAmount());

            // 5) Persist balances
            accountRepository.save(source);
            accountRepository.save(destination);

            // 6) Save SUCCESS log
            TransactionLog successLog = new TransactionLog();
            successLog.setId(txId);
            successLog.setFromAccountId(source.getId());
            successLog.setToAccountId(destination.getId());
            successLog.setAmount(request.getAmount());
            successLog.setStatus(TransactionStatus.SUCCESS);
            successLog.setFailureReason(null);
            successLog.setIdempotencyKey(request.getIdempotencyKey());
            successLog.setCreatedOn(Timestamp.from(Instant.now()));

            transactionLogRepository.save(successLog);

            // 6b) Evaluate and grant reward points to the sender, if eligible
            int points = rewardService.evaluateAndGrant(successLog, source, destination);

            // 7) Build response
            TransferResponse response = new TransferResponse();
            response.setTransactionId(txId.toString());
            response.setStatus("SUCCESS");
            response.setMessage("Transfer completed");
            response.setDebitedFrom(source.getId());
            response.setCreditedTo(destination.getId());
            response.setAmount(request.getAmount());
            response.setRewardPointsEarned(points);
            return response;

        } catch (RuntimeException ex) {
            // 8) Save FAILED log in a separate transaction (so it persists even on rollback)
            transactionLogService.saveFailed(txId, request, ex.getMessage());
            throw ex;
        }
    }

    private boolean isAdminAccount(Account account) {
        if (account.getUser() == null || account.getUser().getRole() == null) {
            return false;
        }
        return "ROLE_ADMIN".equalsIgnoreCase(account.getUser().getRole().trim());
    }
}

