package com.example.moneytransfer.repository;

import com.example.moneytransfer.domain.entity.TransactionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TransactionLogRepository extends JpaRepository<TransactionLog, UUID> {

    // Fetch logs where the account is either sender or receiver
    List<TransactionLog> findByFromAccountIdOrToAccountId(Long fromAccountId, Long toAccountId);

    // Idempotency lookup (prevents duplicates)
    Optional<TransactionLog> findByIdempotencyKey(String idempotencyKey);

    long countByStatus(com.example.moneytransfer.domain.enums.TransactionStatus status);
}