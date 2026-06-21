package com.example.moneytransfer.service;

import com.example.moneytransfer.domain.entity.TransactionLog;
import com.example.moneytransfer.domain.enums.TransactionStatus;
import com.example.moneytransfer.dto.TransferRequest;
import com.example.moneytransfer.repository.TransactionLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;

@Service
public class TransactionLogService {

    private final TransactionLogRepository transactionLogRepository;

    public TransactionLogService(TransactionLogRepository transactionLogRepository) {
        this.transactionLogRepository = transactionLogRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveFailed(UUID txId, TransferRequest request, String reason) {
        TransactionLog log = new TransactionLog();
        log.setId(txId);
        log.setFromAccountId(request.getFromAccountId());
        log.setToAccountId(request.getToAccountId());
        log.setAmount(request.getAmount());
        log.setStatus(TransactionStatus.FAILED);
        log.setFailureReason(reason);
        log.setIdempotencyKey(request.getIdempotencyKey());
        log.setCreatedOn(Timestamp.from(Instant.now()));

        transactionLogRepository.save(log);
    }
}