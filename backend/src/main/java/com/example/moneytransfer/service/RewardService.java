package com.example.moneytransfer.service;

import com.example.moneytransfer.domain.Account;
import com.example.moneytransfer.domain.User;
import com.example.moneytransfer.domain.entity.RewardHistory;
import com.example.moneytransfer.domain.entity.TransactionLog;
import com.example.moneytransfer.domain.enums.TransactionStatus;
import com.example.moneytransfer.dto.RewardHistoryResponse;
import com.example.moneytransfer.dto.RewardSummaryResponse;
import com.example.moneytransfer.repository.RewardHistoryRepository;
import com.example.moneytransfer.repository.UserRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class RewardService {

    private static final BigDecimal MIN_ELIGIBLE_AMOUNT = BigDecimal.valueOf(100);
    private static final BigDecimal POINTS_DIVISOR = BigDecimal.valueOf(100);

    private final RewardHistoryRepository rewardHistoryRepository;
    private final UserRepository userRepository;

    public RewardService(RewardHistoryRepository rewardHistoryRepository, UserRepository userRepository) {
        this.rewardHistoryRepository = rewardHistoryRepository;
        this.userRepository = userRepository;
    }

    /**
     * Evaluates an already-persisted SUCCESS transfer against the reward eligibility rules
     * and grants points to the sender if eligible. No-op (and never throws) when not eligible,
     * so a reward issue can never block the underlying money transfer.
     */
    @Transactional
    public int evaluateAndGrant(TransactionLog transactionLog, Account source, Account destination) {
        if (!isEligible(transactionLog, source, destination)) {
            return 0;
        }
        if (rewardHistoryRepository.existsByTransactionId(transactionLog.getId())) {
            return 0;
        }

        int points = transactionLog.getAmount()
                .divideToIntegralValue(POINTS_DIVISOR)
                .intValue();
        if (points <= 0) {
            return 0;
        }

        RewardHistory reward = new RewardHistory();
        reward.setId(UUID.randomUUID());
        reward.setUserId(source.getUser().getId());
        reward.setAccountId(source.getId());
        reward.setTransactionId(transactionLog.getId());
        reward.setAmount(transactionLog.getAmount());
        reward.setPointsEarned(points);
        reward.setCreatedOn(Timestamp.from(Instant.now()));

        rewardHistoryRepository.save(reward);
        return points;
    }

    private boolean isEligible(TransactionLog transactionLog, Account source, Account destination) {
        if (transactionLog.getStatus() != TransactionStatus.SUCCESS) {
            return false;
        }
        if (transactionLog.getAmount() == null
                || transactionLog.getAmount().compareTo(MIN_ELIGIBLE_AMOUNT) < 0) {
            return false;
        }
        if (source.getId().equals(destination.getId())) {
            return false;
        }
        return !source.getUser().getId().equals(destination.getUser().getId());
    }

    public int getPointsForTransaction(UUID transactionId) {
        return rewardHistoryRepository.findByTransactionId(transactionId)
                .map(RewardHistory::getPointsEarned)
                .orElse(0);
    }

    @Transactional(readOnly = true)
    public List<RewardHistoryResponse> getHistory(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        return rewardHistoryRepository.findByUserIdOrderByCreatedOnDesc(user.getId()).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public RewardSummaryResponse getSummary(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        RewardSummaryResponse response = new RewardSummaryResponse();
        response.setUserId(user.getId());
        response.setTotalPoints(rewardHistoryRepository.sumPointsEarnedByUserId(user.getId()));
        return response;
    }

    private RewardHistoryResponse toResponse(RewardHistory reward) {
        RewardHistoryResponse response = new RewardHistoryResponse();
        response.setId(reward.getId());
        response.setAccountId(reward.getAccountId());
        response.setTransactionId(reward.getTransactionId());
        response.setAmount(reward.getAmount());
        response.setPointsEarned(reward.getPointsEarned());
        response.setCreatedOn(reward.getCreatedOn());
        return response;
    }
}
