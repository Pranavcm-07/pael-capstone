package com.example.moneytransfer.repository;

import com.example.moneytransfer.domain.entity.AccountStatusAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccountStatusAuditRepository extends JpaRepository<AccountStatusAudit, Long> {
    List<AccountStatusAudit> findByAccountId(Long accountId);
}
