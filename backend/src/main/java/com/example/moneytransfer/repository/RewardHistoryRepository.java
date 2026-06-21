package com.example.moneytransfer.repository;

import com.example.moneytransfer.domain.entity.RewardHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RewardHistoryRepository extends JpaRepository<RewardHistory, UUID> {

    List<RewardHistory> findByUserIdOrderByCreatedOnDesc(Long userId);

    boolean existsByTransactionId(UUID transactionId);

    Optional<RewardHistory> findByTransactionId(UUID transactionId);

    @Query("select coalesce(sum(r.pointsEarned), 0) from RewardHistory r where r.userId = :userId")
    Integer sumPointsEarnedByUserId(@Param("userId") Long userId);

    @Query("SELECT COALESCE(SUM(r.pointsEarned), 0) FROM RewardHistory r")
    Integer sumAllPointsEarned();
}
