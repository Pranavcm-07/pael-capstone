package com.example.moneytransfer.service;

import com.example.moneytransfer.domain.Account;
import com.example.moneytransfer.domain.User;
import com.example.moneytransfer.domain.enums.AccountStatus;
import com.example.moneytransfer.dto.AccountResponse;
import com.example.moneytransfer.exception.AccountNotFoundException;
import com.example.moneytransfer.exception.UnauthorizedAccessException;
import com.example.moneytransfer.repository.AccountRepository;
import com.example.moneytransfer.repository.AccountStatusAuditRepository;
import com.example.moneytransfer.repository.TransactionLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionLogRepository transactionLogRepository;

    @Mock
    private AccountStatusAuditRepository accountStatusAuditRepository;

    @InjectMocks
    private AccountService accountService;

    private User testUser;
    private Account testAccount;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUsername("johndoe");

        testAccount = new Account();
        testAccount.setId(1L);
        testAccount.setUser(testUser);
        testAccount.setHolderName("John Doe");
        testAccount.setBalance(BigDecimal.valueOf(1000));
        testAccount.setStatus(AccountStatus.ACTIVE);
    }

    @Test
    void getAccount_Success() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(testAccount));

        AccountResponse response = accountService.getAccount(1L, "johndoe");

        assertNotNull(response);
        assertEquals("John Doe", response.getHolderName());
        assertEquals(BigDecimal.valueOf(1000), response.getBalance());
    }

    @Test
    void getAccount_UnauthorizedThrowsException() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(testAccount));

        assertThrows(UnauthorizedAccessException.class, () -> 
            accountService.getAccount(1L, "wronguser")
        );
    }

    @Test
    void getAccount_NotFoundThrowsException() {
        when(accountRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class, () -> 
            accountService.getAccount(1L, "johndoe")
        );
    }

    @Test
    void updateAccountStatus_AuditLogged() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(testAccount));

        accountService.updateAccountStatus(1L, AccountStatus.LOCKED, "Suspicious activity", "admin");

        verify(accountRepository).save(testAccount);
        verify(accountStatusAuditRepository).save(any());
        assertEquals(AccountStatus.LOCKED, testAccount.getStatus());
    }
}
