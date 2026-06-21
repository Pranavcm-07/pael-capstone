package com.example.moneytransfer.controller;

import com.example.moneytransfer.dto.RewardHistoryResponse;
import com.example.moneytransfer.dto.RewardSummaryResponse;
import com.example.moneytransfer.service.RewardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/rewards")
public class RewardController {

    private final RewardService rewardService;

    public RewardController(RewardService rewardService) {
        this.rewardService = rewardService;
    }

    @GetMapping("/history")
    public ResponseEntity<List<RewardHistoryResponse>> getHistory(Principal principal) {
        return ResponseEntity.ok(rewardService.getHistory(principal.getName()));
    }

    @GetMapping("/summary")
    public ResponseEntity<RewardSummaryResponse> getSummary(Principal principal) {
        return ResponseEntity.ok(rewardService.getSummary(principal.getName()));
    }
}
