package com.example.moneytransfer.service;

import com.example.moneytransfer.domain.Account;
import com.example.moneytransfer.domain.User;
import com.example.moneytransfer.domain.enums.AccountStatus;
import com.example.moneytransfer.dto.TransferRequest;
import com.example.moneytransfer.repository.AccountRepository;
import com.example.moneytransfer.repository.TransactionLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransferServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionLogRepository transactionLogRepository;

    @Mock
    private TransactionLogService transactionLogService;

    @Mock
    private RewardService rewardService;

    @Mock
    private SystemSettingService systemSettingService;

    @InjectMocks
    private TransferService transferService;

    private Account source;
    private Account adminDestination;
    private TransferRequest request;

    @BeforeEach
    void setUp() {
        User sender = new User();
        sender.setId(1L);
        sender.setUsername("johndoe");
        sender.setRole("ROLE_USER");

        User admin = new User();
        admin.setId(2L);
        admin.setUsername("admin");
        admin.setRole("role_admin");

        source = new Account();
        source.setId(10L);
        source.setUser(sender);
        source.setHolderName("John Doe");
        source.setBalance(BigDecimal.valueOf(1000));
        source.setStatus(AccountStatus.ACTIVE);

        adminDestination = new Account();
        adminDestination.setId(20L);
        adminDestination.setUser(admin);
        adminDestination.setHolderName("Admin User");
        adminDestination.setBalance(BigDecimal.valueOf(500));
        adminDestination.setStatus(AccountStatus.ACTIVE);

        request = new TransferRequest();
        request.setFromAccountId(source.getId());
        request.setToAccountId(adminDestination.getId());
        request.setAmount(BigDecimal.valueOf(100));
        request.setIdempotencyKey("tx-1");
    }

    @Test
    void transfer_ToAdminAccountThrowsAndDoesNotPersistBalances() {
        when(transactionLogRepository.findByIdempotencyKey("tx-1")).thenReturn(Optional.empty());
        when(accountRepository.findById(source.getId())).thenReturn(Optional.of(source));
        when(accountRepository.findById(adminDestination.getId())).thenReturn(Optional.of(adminDestination));
        when(systemSettingService.isTransfersEnabled()).thenReturn(true);
        when(systemSettingService.getMinTransferAmount()).thenReturn(BigDecimal.valueOf(0.01));
        when(systemSettingService.getMaxTransferAmount()).thenReturn(BigDecimal.valueOf(10000));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> transferService.transfer(request, "johndoe"));

        assertEquals("VAL-422:Cannot transfer money to an administrator account", ex.getMessage());
        assertEquals(BigDecimal.valueOf(1000), source.getBalance());
        assertEquals(BigDecimal.valueOf(500), adminDestination.getBalance());
        verify(accountRepository, never()).save(any(Account.class));
        verify(transactionLogService).saveFailed(any(UUID.class), eq(request), contains("administrator account"));
    }
}
