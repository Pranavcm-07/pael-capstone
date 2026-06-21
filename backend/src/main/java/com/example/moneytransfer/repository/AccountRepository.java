package com.example.moneytransfer.repository;

import com.example.moneytransfer.domain.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface AccountRepository extends JpaRepository<Account, Long> {
    List<Account> findByUserId(Long userId);
    List<Account> findByUserUsername(String username);

    @Query("SELECT COALESCE(SUM(a.balance), 0) FROM Account a")
    java.math.BigDecimal sumAllBalances();
}

