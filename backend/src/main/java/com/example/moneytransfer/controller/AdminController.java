package com.example.moneytransfer.controller;

import com.example.moneytransfer.domain.Account;
import com.example.moneytransfer.domain.entity.RewardHistory;
import com.example.moneytransfer.domain.entity.TransactionLog;
import com.example.moneytransfer.repository.AccountRepository;
import com.example.moneytransfer.repository.RewardHistoryRepository;
import com.example.moneytransfer.repository.TransactionLogRepository;
import com.example.moneytransfer.repository.UserRepository;
import com.example.moneytransfer.repository.AccountStatusAuditRepository;
import com.example.moneytransfer.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final TransactionLogRepository transactionLogRepository;
    private final RewardHistoryRepository rewardHistoryRepository;
    private final AccountStatusAuditRepository accountStatusAuditRepository;
    private final AccountService accountService;
    private final DataSource dataSource;
    private final com.example.moneytransfer.service.SystemSettingService systemSettingService;

    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers() {
        List<Map<String, Object>> userList = userRepository.findAll().stream()
                .map(u -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", u.getId());
                    map.put("username", u.getUsername());
                    map.put("createdAt", u.getCreatedAt() != null ? u.getCreatedAt().toString() : "");
                    map.put("role", u.getRole());
                    return map;
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(userList);
    }

    @GetMapping("/accounts")
    public ResponseEntity<?> getAllAccounts() {
        List<Map<String, Object>> accountList = accountRepository.findAll().stream()
                .map(acc -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", acc.getId());
                    map.put("userId", acc.getUser() != null ? acc.getUser().getId() : null);
                    map.put("username", acc.getUser() != null ? acc.getUser().getUsername() : "N/A");
                    map.put("holderName", acc.getHolderName());
                    map.put("balance", acc.getBalance());
                    map.put("status", acc.getStatus().name());
                    map.put("lastUpdated", acc.getLastUpdated() != null ? acc.getLastUpdated().toString() : "");
                    return map;
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(accountList);
    }

    @GetMapping("/transactions")
    public ResponseEntity<?> getAllTransactions() {
        List<TransactionLog> logs = transactionLogRepository.findAll();
        List<Long> accountIds = logs.stream()
                .flatMap(log -> List.of(log.getFromAccountId() != null ? log.getFromAccountId() : -1L,
                                        log.getToAccountId() != null ? log.getToAccountId() : -1L).stream())
                .filter(id -> id != -1L)
                .distinct()
                .toList();
        Map<Long, Account> accountMap = accountRepository.findAllById(accountIds).stream()
                .collect(Collectors.toMap(Account::getId, acc -> acc));

        List<Map<String, Object>> txList = logs.stream()
                .map(log -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", log.getId().toString());
                    map.put("fromAccountId", log.getFromAccountId());
                    Account from = accountMap.get(log.getFromAccountId());
                    map.put("fromAccountHolderName", from != null ? from.getHolderName() : "N/A");
                    map.put("toAccountId", log.getToAccountId());
                    Account to = accountMap.get(log.getToAccountId());
                    map.put("toAccountHolderName", to != null ? to.getHolderName() : "N/A");
                    map.put("amount", log.getAmount());
                    map.put("status", log.getStatus().name());
                    map.put("failureReason", log.getFailureReason());
                    map.put("idempotencyKey", log.getIdempotencyKey());
                    map.put("createdOn", log.getCreatedOn() != null ? log.getCreatedOn().toString() : "");
                    return map;
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(txList);
    }

    @GetMapping("/stats")
    public ResponseEntity<?> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", userRepository.count());
        stats.put("totalAccounts", accountRepository.count());
        stats.put("totalBalances", accountRepository.sumAllBalances());
        stats.put("totalTransactions", transactionLogRepository.count());
        stats.put("successTransactions", transactionLogRepository.countByStatus(com.example.moneytransfer.domain.enums.TransactionStatus.SUCCESS));
        stats.put("failedTransactions", transactionLogRepository.countByStatus(com.example.moneytransfer.domain.enums.TransactionStatus.FAILED));
        stats.put("totalRewardPoints", rewardHistoryRepository.sumAllPointsEarned());
        return ResponseEntity.ok(stats);
    }

    @PutMapping("/accounts/{id}/status")
    public ResponseEntity<?> updateAccountStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            java.security.Principal principal) {

        String statusStr = body.get("status");
        String reason = body.getOrDefault("reason", "Admin operation");

        com.example.moneytransfer.domain.enums.AccountStatus status;
        try {
            status = com.example.moneytransfer.domain.enums.AccountStatus.valueOf(statusStr.toUpperCase());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Invalid status: " + statusStr));
        }

        try {
            accountService.updateAccountStatus(id, status, reason, principal.getName());
            return ResponseEntity.ok(Map.of("success", true, "message", "Account status updated to " + status));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/users/{id}/role")
    public ResponseEntity<?> updateUserRole(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String role = body.get("role");
        if (role == null || (!role.equals("ROLE_USER") && !role.equals("ROLE_ADMIN"))) {
            return ResponseEntity.badRequest().body(Map.of("message", "Invalid role. Must be ROLE_USER or ROLE_ADMIN."));
        }

        return userRepository.findById(id).map(u -> {
            u.setRole(role);
            userRepository.save(u);
            return ResponseEntity.ok(Map.of("success", true, "message", "User role updated successfully to " + role));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/accounts/{id}/balance")
    public ResponseEntity<?> updateAccountBalance(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Object balanceObj = body.get("balance");
        if (balanceObj == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Balance is required."));
        }

        java.math.BigDecimal balance;
        try {
            balance = new java.math.BigDecimal(balanceObj.toString());
            if (balance.compareTo(java.math.BigDecimal.ZERO) < 0) {
                return ResponseEntity.badRequest().body(Map.of("message", "Balance cannot be negative."));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Invalid balance value."));
        }

        return accountRepository.findById(id).map(acc -> {
            acc.setBalance(balance);
            accountRepository.save(acc);
            return ResponseEntity.ok(Map.of("success", true, "message", "Account balance updated successfully."));
        }).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/audits")
    public ResponseEntity<?> getAudits() {
        List<com.example.moneytransfer.domain.entity.AccountStatusAudit> audits = accountStatusAuditRepository.findAll();
        audits.sort((a, b) -> b.getCreatedOn().compareTo(a.getCreatedOn()));
        return ResponseEntity.ok(audits);
    }

    @PostMapping("/query")
    public ResponseEntity<?> executeQuery(@RequestBody Map<String, String> body) {
        String sql = body.get("query");
        if (sql == null || sql.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Query cannot be empty"));
        }

        String normalized = sql.trim().toLowerCase();
        if (!normalized.startsWith("select")) {
            return ResponseEntity.badRequest().body(Map.of("error", "Only SELECT queries are allowed for security."));
        }
        if (normalized.contains("delete ") || normalized.contains("update ") || normalized.contains("insert ") || 
            normalized.contains("drop ") || normalized.contains("alter ") || normalized.contains("truncate ") || 
            normalized.contains("create ") || normalized.contains("replace ") || normalized.contains("grant ") || 
            normalized.contains("revoke ")) {
            return ResponseEntity.badRequest().body(Map.of("error", "Modification queries are blocked."));
        }

        try (Connection connection = dataSource.getConnection();
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            List<String> columns = new ArrayList<>();
            for (int i = 1; i <= columnCount; i++) {
                columns.add(metaData.getColumnName(i));
            }

            List<Map<String, Object>> rows = new ArrayList<>();
            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    row.put(metaData.getColumnName(i), rs.getObject(i));
                }
                rows.add(row);
            }

            Map<String, Object> result = new HashMap<>();
            result.put("columns", columns);
            result.put("rows", rows);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/db-metadata")
    public ResponseEntity<?> getDbMetadata() {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            Map<String, Object> dbInfo = new HashMap<>();
            dbInfo.put("databaseProductName", metaData.getDatabaseProductName());
            dbInfo.put("databaseProductVersion", metaData.getDatabaseProductVersion());
            dbInfo.put("driverName", metaData.getDriverName());
            dbInfo.put("driverVersion", metaData.getDriverVersion());
            dbInfo.put("url", metaData.getURL());
            dbInfo.put("username", metaData.getUserName());

            List<Map<String, Object>> tables = new ArrayList<>();
            try (ResultSet rs = metaData.getTables(null, null, "%", new String[]{"TABLE"})) {
                while (rs.next()) {
                    String tableName = rs.getString("TABLE_NAME");
                    if (tableName.equalsIgnoreCase("users") ||
                            tableName.equalsIgnoreCase("accounts") ||
                            tableName.equalsIgnoreCase("transaction_logs") ||
                            tableName.equalsIgnoreCase("reward_history") ||
                            tableName.equalsIgnoreCase("account_status_audit")) {

                        Map<String, Object> tableMap = new HashMap<>();
                        tableMap.put("tableName", tableName);

                        try (Statement stmt = connection.createStatement();
                             ResultSet countRs = stmt.executeQuery("SELECT COUNT(*) FROM " + tableName)) {
                            if (countRs.next()) {
                                tableMap.put("rowCount", countRs.getLong(1));
                            } else {
                                tableMap.put("rowCount", 0L);
                            }
                        } catch (Exception ex) {
                            tableMap.put("rowCount", -1L);
                        }
                        tables.add(tableMap);
                    }
                }
            }
            dbInfo.put("tables", tables);

            return ResponseEntity.ok(dbInfo);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/settings")
    public ResponseEntity<?> getSettings() {
        return ResponseEntity.ok(systemSettingService.getAllSettings());
    }

    @PutMapping("/settings")
    public ResponseEntity<?> updateSettings(@RequestBody Map<String, Object> body) {
        systemSettingService.updateSettings(body);
        return ResponseEntity.ok(Map.of("success", true, "message", "System settings updated successfully."));
    }

    @PostMapping("/accounts/{id}/add")
    public ResponseEntity<?> addMoney(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Object amountObj = body.get("amount");
        if (amountObj == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Amount is required."));
        }
        java.math.BigDecimal amount;
        try {
            amount = new java.math.BigDecimal(amountObj.toString());
            if (amount.compareTo(java.math.BigDecimal.ZERO) <= 0) {
                return ResponseEntity.badRequest().body(Map.of("message", "Amount must be positive."));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Invalid amount value."));
        }

        return accountRepository.findById(id).map(acc -> {
            acc.credit(amount);
            accountRepository.save(acc);
            return ResponseEntity.ok(Map.of("success", true, "message", "Successfully credited $" + amount));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/accounts/{id}/deduct")
    public ResponseEntity<?> deductMoney(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Object amountObj = body.get("amount");
        if (amountObj == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Amount is required."));
        }
        java.math.BigDecimal amount;
        try {
            amount = new java.math.BigDecimal(amountObj.toString());
            if (amount.compareTo(java.math.BigDecimal.ZERO) <= 0) {
                return ResponseEntity.badRequest().body(Map.of("message", "Amount must be positive."));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Invalid amount value."));
        }

        return accountRepository.findById(id).map(acc -> {
            try {
                acc.debit(amount);
                accountRepository.save(acc);
                return ResponseEntity.ok(Map.of("success", true, "message", "Successfully debited $" + amount));
            } catch (Exception e) {
                return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
            }
        }).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/reports")
    public ResponseEntity<?> getReports() {
        Map<String, Object> report = new HashMap<>();

        long totalTransfers = transactionLogRepository.countByStatus(com.example.moneytransfer.domain.enums.TransactionStatus.SUCCESS);
        report.put("totalTransfers", totalTransfers);

        java.math.BigDecimal totalMoneyMoved = transactionLogRepository.findAll().stream()
                .filter(log -> log.getStatus() == com.example.moneytransfer.domain.enums.TransactionStatus.SUCCESS)
                .map(com.example.moneytransfer.domain.entity.TransactionLog::getAmount)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
        report.put("totalMoneyMoved", totalMoneyMoved);

        Map<String, Map<String, Object>> dailyGroups = new TreeMap<>();
        transactionLogRepository.findAll().stream()
                .filter(log -> log.getStatus() == com.example.moneytransfer.domain.enums.TransactionStatus.SUCCESS)
                .forEach(log -> {
                    java.sql.Timestamp createdOn = log.getCreatedOn();
                    if (createdOn != null) {
                        String dateStr = new java.text.SimpleDateFormat("yyyy-MM-dd").format(createdOn);
                        dailyGroups.computeIfAbsent(dateStr, k -> {
                            Map<String, Object> m = new HashMap<>();
                            m.put("date", k);
                            m.put("count", 0L);
                            m.put("amount", java.math.BigDecimal.ZERO);
                            return m;
                        });
                        Map<String, Object> dayMap = dailyGroups.get(dateStr);
                        dayMap.put("count", (Long) dayMap.get("count") + 1);
                        dayMap.put("amount", ((java.math.BigDecimal) dayMap.get("amount")).add(log.getAmount()));
                    }
                });

        report.put("dailySummary", new ArrayList<>(dailyGroups.values()));

        return ResponseEntity.ok(report);
    }

    @GetMapping("/rewards")
    public ResponseEntity<?> getUsersRewards() {
        List<Map<String, Object>> list = userRepository.findAll().stream()
                .map(u -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", u.getId());
                    map.put("username", u.getUsername());
                    
                    List<Account> accounts = accountRepository.findAll().stream()
                            .filter(acc -> acc.getUser() != null && acc.getUser().getId().equals(u.getId()))
                            .toList();
                    String holderName = accounts.isEmpty() ? "N/A" : accounts.get(0).getHolderName();
                    Long accountId = accounts.isEmpty() ? null : accounts.get(0).getId();
                    
                    map.put("holderName", holderName);
                    map.put("accountId", accountId);
                    
                    Integer points = rewardHistoryRepository.sumPointsEarnedByUserId(u.getId());
                    map.put("points", points != null ? points : 0);
                    return map;
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    @PostMapping("/users/{id}/adjust-rewards")
    @org.springframework.transaction.annotation.Transactional
    public ResponseEntity<?> adjustRewards(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Object pointsObj = body.get("points");
        if (pointsObj == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Points value is required."));
        }
        int points;
        try {
            points = Integer.parseInt(pointsObj.toString());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Invalid points value. Must be an integer."));
        }

        return userRepository.findById(id).map(user -> {
            List<Account> userAccounts = accountRepository.findAll().stream()
                    .filter(acc -> acc.getUser() != null && acc.getUser().getId().equals(user.getId()))
                    .toList();
            if (userAccounts.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "No account found for user."));
            }
            Account targetAccount = userAccounts.get(0);

            // Create a dummy transaction log for the adjustment to satisfy the FK constraint
            TransactionLog dummyTx = new TransactionLog();
            dummyTx.setId(UUID.randomUUID());
            dummyTx.setFromAccountId(null);
            dummyTx.setToAccountId(targetAccount.getId());
            dummyTx.setAmount(java.math.BigDecimal.ZERO);
            dummyTx.setStatus(com.example.moneytransfer.domain.enums.TransactionStatus.SUCCESS);
            dummyTx.setFailureReason("Reward adjustment: " + (points >= 0 ? "+" : "") + points + " points");
            dummyTx.setIdempotencyKey(UUID.randomUUID().toString());
            dummyTx.setCreatedOn(new java.sql.Timestamp(System.currentTimeMillis()));
            transactionLogRepository.save(dummyTx);

            // Create RewardHistory entry
            RewardHistory reward = new RewardHistory();
            reward.setId(UUID.randomUUID());
            reward.setUserId(user.getId());
            reward.setAccountId(targetAccount.getId());
            reward.setTransactionId(dummyTx.getId());
            reward.setAmount(java.math.BigDecimal.ZERO);
            reward.setPointsEarned(points);
            reward.setCreatedOn(new java.sql.Timestamp(System.currentTimeMillis()));
            rewardHistoryRepository.save(reward);

            return ResponseEntity.ok(Map.of("success", true, "message", "Successfully adjusted rewards by " + points + " points."));
        }).orElse(ResponseEntity.notFound().build());
    }
}
