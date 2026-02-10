package com.example.moneytransfer.domain;

import com.example.moneytransfer.enums.AccountStatus;
import com.example.moneytransfer.exception.AccountNotActiveException;
import com.example.moneytransfer.exception.InsufficientBalanceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Account Domain Entity Tests")
class AccountTest {

    private Account account;

    @BeforeEach
    void setUp() {
        account = new Account(1L, "John Doe", new BigDecimal("1000.00"), "password", AccountStatus.ACTIVE);
    }

    @Nested
    @DisplayName("Debit Operations")
    class DebitOperations {

        @Test
        @DisplayName("Should successfully debit when balance is sufficient and account is active")
        void testDebitSuccess() {
            BigDecimal initialBalance = account.getBalance();
            BigDecimal debitAmount = new BigDecimal("300.00");
            BigDecimal expectedBalance = new BigDecimal("700.00");

            account.debit(debitAmount);

            assertThat(account.getBalance())
                    .isEqualByComparingTo(expectedBalance);
            assertThat(account.getLastUpdated()).isNotNull();
        }

        @Test
        @DisplayName("Should successfully debit exact balance amount")
        void testDebitExactBalance() {
            BigDecimal debitAmount = new BigDecimal("1000.00");

            account.debit(debitAmount);

            assertThat(account.getBalance())
                    .isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("Should throw InsufficientBalanceException when balance is less than debit amount")
        void testDebitInsufficientBalance() {
            BigDecimal debitAmount = new BigDecimal("1500.00");

            assertThatThrownBy(() -> account.debit(debitAmount))
                    .isInstanceOf(InsufficientBalanceException.class)
                    .hasMessageContaining("Insufficient balance")
                    .hasMessageContaining("1000")
                    .hasMessageContaining("1500");
        }

        @Test
        @DisplayName("Should throw AccountNotActiveException when debiting locked account")
        void testDebitOnLockedAccount() {
            account.setStatus(AccountStatus.LOCKED);
            BigDecimal debitAmount = new BigDecimal("100.00");

            assertThatThrownBy(() -> account.debit(debitAmount))
                    .isInstanceOf(AccountNotActiveException.class)
                    .hasMessageContaining("not active")
                    .hasMessageContaining("Locked");
        }

        @Test
        @DisplayName("Should throw AccountNotActiveException when debiting closed account")
        void testDebitOnClosedAccount() {
            account.setStatus(AccountStatus.CLOSED);
            BigDecimal debitAmount = new BigDecimal("100.00");

            assertThatThrownBy(() -> account.debit(debitAmount))
                    .isInstanceOf(AccountNotActiveException.class)
                    .hasMessageContaining("not active")
                    .hasMessageContaining("Closed");
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when debit amount is null")
        void testDebitNullAmount() {
            assertThatThrownBy(() -> account.debit(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Amount cannot be null");
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when debit amount is zero")
        void testDebitZeroAmount() {
            assertThatThrownBy(() -> account.debit(BigDecimal.ZERO))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Amount must be greater than zero");
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when debit amount is negative")
        void testDebitNegativeAmount() {
            assertThatThrownBy(() -> account.debit(new BigDecimal("-100.00")))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Amount must be greater than zero");
        }
    }

    @Nested
    @DisplayName("Credit Operations")
    class CreditOperations {

        @Test
        @DisplayName("Should successfully credit when account is active")
        void testCreditSuccess() {
            BigDecimal creditAmount = new BigDecimal("500.00");
            BigDecimal expectedBalance = new BigDecimal("1500.00");

            account.credit(creditAmount);

            assertThat(account.getBalance())
                    .isEqualByComparingTo(expectedBalance);
            assertThat(account.getLastUpdated()).isNotNull();
        }

        @Test
        @DisplayName("Should successfully credit small amounts")
        void testCreditSmallAmount() {
            BigDecimal creditAmount = new BigDecimal("0.01");
            BigDecimal expectedBalance = new BigDecimal("1000.01");

            account.credit(creditAmount);

            assertThat(account.getBalance())
                    .isEqualByComparingTo(expectedBalance);
        }

        @Test
        @DisplayName("Should throw AccountNotActiveException when crediting locked account")
        void testCreditOnLockedAccount() {
            account.setStatus(AccountStatus.LOCKED);
            BigDecimal creditAmount = new BigDecimal("100.00");

            assertThatThrownBy(() -> account.credit(creditAmount))
                    .isInstanceOf(AccountNotActiveException.class)
                    .hasMessageContaining("not active");
        }

        @Test
        @DisplayName("Should throw AccountNotActiveException when crediting closed account")
        void testCreditOnClosedAccount() {
            account.setStatus(AccountStatus.CLOSED);
            BigDecimal creditAmount = new BigDecimal("100.00");

            assertThatThrownBy(() -> account.credit(creditAmount))
                    .isInstanceOf(AccountNotActiveException.class)
                    .hasMessageContaining("not active");
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when credit amount is null")
        void testCreditNullAmount() {
            assertThatThrownBy(() -> account.credit(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Amount cannot be null");
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when credit amount is zero")
        void testCreditZeroAmount() {
            assertThatThrownBy(() -> account.credit(BigDecimal.ZERO))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Amount must be greater than zero");
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when credit amount is negative")
        void testCreditNegativeAmount() {
            assertThatThrownBy(() -> account.credit(new BigDecimal("-100.00")))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Amount must be greater than zero");
        }
    }

    @Nested
    @DisplayName("Account Status")
    class AccountStatusTests {

        @Test
        @DisplayName("Should return true for isActive when status is ACTIVE")
        void testIsActiveWhenActive() {
            account.setStatus(AccountStatus.ACTIVE);

            assertThat(account.isActive()).isTrue();
        }

        @Test
        @DisplayName("Should return false for isActive when status is LOCKED")
        void testIsActiveWhenLocked() {
            account.setStatus(AccountStatus.LOCKED);

            assertThat(account.isActive()).isFalse();
        }

        @Test
        @DisplayName("Should return false for isActive when status is CLOSED")
        void testIsActiveWhenClosed() {
            account.setStatus(AccountStatus.CLOSED);

            assertThat(account.isActive()).isFalse();
        }
    }

    @Nested
    @DisplayName("Version Management")
    class VersionManagement {

        @Test
        @DisplayName("Should increment version correctly")
        void testIncrementVersion() {
            account.setVersion(0);

            account.incrementVersion();

            assertThat(account.getVersion()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should handle null version when incrementing")
        void testIncrementNullVersion() {
            account.setVersion(null);

            account.incrementVersion();

            assertThat(account.getVersion()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("Account Construction")
    class AccountConstruction {

        @Test
        @DisplayName("Should create account with default values using default constructor")
        void testDefaultConstructor() {
            Account newAccount = new Account();

            assertThat(newAccount.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(newAccount.getStatus()).isEqualTo(AccountStatus.ACTIVE);
            assertThat(newAccount.getVersion()).isEqualTo(0);
            assertThat(newAccount.getLastUpdated()).isNotNull();
        }

        @Test
        @DisplayName("Should create account with specified values")
        void testParameterizedConstructor() {
            Account newAccount = new Account(1L, "Jane Doe",
                    new BigDecimal("500.00"), "password", AccountStatus.ACTIVE);

            assertThat(newAccount.getId()).isEqualTo(1L);
            assertThat(newAccount.getHolderName()).isEqualTo("Jane Doe");
            assertThat(newAccount.getBalance()).isEqualByComparingTo(new BigDecimal("500.00"));
            assertThat(newAccount.getStatus()).isEqualTo(AccountStatus.ACTIVE);
        }

        @Test
        @DisplayName("Should handle null balance in constructor")
        void testConstructorWithNullBalance() {
            Account newAccount = new Account(1L, "Jane Doe", null, "password", AccountStatus.ACTIVE);

            assertThat(newAccount.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("Should handle null status in constructor")
        void testConstructorWithNullStatus() {
            Account newAccount = new Account(1L, "Jane Doe",
                    new BigDecimal("500.00"), "password", null);

            assertThat(newAccount.getStatus()).isEqualTo(AccountStatus.ACTIVE);
        }
    }

    @Nested
    @DisplayName("Object Methods")
    class ObjectMethods {

        @Test
        @DisplayName("Should be equal when IDs match")
        void testEqualsWithSameId() {
            Account account1 = new Account(1L, "John", new BigDecimal("100"), "password", AccountStatus.ACTIVE);
            Account account2 = new Account(1L, "Jane", new BigDecimal("200"), "password", AccountStatus.LOCKED);

            assertThat(account1).isEqualTo(account2);
            assertThat(account1.hashCode()).isEqualTo(account2.hashCode());
        }

        @Test
        @DisplayName("Should not be equal when IDs differ")
        void testEqualsWithDifferentId() {
            Account account1 = new Account(1L, "John", new BigDecimal("100"), "password", AccountStatus.ACTIVE);
            Account account2 = new Account(2L, "John", new BigDecimal("100"), "password", AccountStatus.ACTIVE);

            assertThat(account1).isNotEqualTo(account2);
        }

        @Test
        @DisplayName("Should generate meaningful toString")
        void testToString() {
            String toString = account.toString();
            assertThat(toString).contains("id=1");
            assertThat(toString).contains("holderName='John Doe'");
            assertThat(toString).contains("balance=1000.00");
            assertThat(toString).contains("status=ACTIVE");
        }
    }
}
