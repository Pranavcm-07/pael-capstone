package com.example.moneytransfer.repository;

import com.example.moneytransfer.domain.TransactionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TransactionLogRepository extends JpaRepository<TransactionLog, UUID> {

    Optional<TransactionLog> findByIdempotencyKey(String idempotencyKey);

    List<TransactionLog> findByFromAccountIdOrToAccountIdOrderByCreatedOnDesc(Long fromAccountId, Long toAccountId);
}
