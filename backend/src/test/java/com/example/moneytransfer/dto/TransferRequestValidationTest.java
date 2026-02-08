package com.example.moneytransfer.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("TransferRequest Validation Tests")
class TransferRequestValidationTest {

        private static Validator validator;

        @BeforeAll
        static void setUp() {
                ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
                validator = factory.getValidator();
        }

        @Nested
        @DisplayName("Valid Request Tests")
        class ValidRequestTests {

                @Test
                @DisplayName("Should pass validation for valid transfer request")
                void validRequestPasses() {
                        TransferRequest request = new TransferRequest(
                                        1L,
                                        2L,
                                        new BigDecimal("100.00"),
                                        "unique-key-123");

                        Set<ConstraintViolation<TransferRequest>> violations = validator.validate(request);

                        assertThat(violations).isEmpty();
                }

                @Test
                @DisplayName("Should pass validation for minimum amount of 0.01")
                void validRequestWithMinimumAmount() {
                        TransferRequest request = new TransferRequest(
                                        1L,
                                        2L,
                                        new BigDecimal("0.01"),
                                        "unique-key-456");

                        Set<ConstraintViolation<TransferRequest>> violations = validator.validate(request);

                        assertThat(violations).isEmpty();
                }

                @Test
                @DisplayName("Should pass validation for large amount")
                void validRequestWithLargeAmount() {
                        TransferRequest request = new TransferRequest(
                                        1L,
                                        2L,
                                        new BigDecimal("999999999.99"),
                                        "unique-key-789");

                        Set<ConstraintViolation<TransferRequest>> violations = validator.validate(request);

                        assertThat(violations).isEmpty();
                }
        }

        @Nested
        @DisplayName("Null Field Validation Tests")
        class NullFieldTests {

                @Test
                @DisplayName("Should fail validation when fromAccountId is null")
                void nullFromAccountIdFails() {
                        TransferRequest request = new TransferRequest(
                                        null,
                                        2L,
                                        new BigDecimal("100.00"),
                                        "unique-key-123");

                        Set<ConstraintViolation<TransferRequest>> violations = validator.validate(request);

                        assertThat(violations)
                                        .hasSize(1)
                                        .extracting(ConstraintViolation::getMessage)
                                        .containsExactly("Source account ID is required");
                }

                @Test
                @DisplayName("Should fail validation when toAccountId is null")
                void nullToAccountIdFails() {
                        TransferRequest request = new TransferRequest(
                                        1L,
                                        null,
                                        new BigDecimal("100.00"),
                                        "unique-key-123");

                        Set<ConstraintViolation<TransferRequest>> violations = validator.validate(request);

                        assertThat(violations)
                                        .hasSize(1)
                                        .extracting(ConstraintViolation::getMessage)
                                        .containsExactly("Destination account ID is required");
                }

                @Test
                @DisplayName("Should fail validation when amount is null")
                void nullAmountFails() {
                        TransferRequest request = new TransferRequest(
                                        1L,
                                        2L,
                                        null,
                                        "unique-key-123");

                        Set<ConstraintViolation<TransferRequest>> violations = validator.validate(request);

                        assertThat(violations)
                                        .hasSize(1)
                                        .extracting(ConstraintViolation::getMessage)
                                        .containsExactly("Transfer amount is required");
                }

                @Test
                @DisplayName("Should fail validation when idempotencyKey is null")
                void nullIdempotencyKeyFails() {
                        TransferRequest request = new TransferRequest(
                                        1L,
                                        2L,
                                        new BigDecimal("100.00"),
                                        null);

                        Set<ConstraintViolation<TransferRequest>> violations = validator.validate(request);

                        assertThat(violations)
                                        .hasSize(1)
                                        .extracting(ConstraintViolation::getMessage)
                                        .containsExactly("Idempotency key is required");
                }

                @Test
                @DisplayName("Should fail validation when all fields are null")
                void nullFieldsFail() {
                        TransferRequest request = new TransferRequest(
                                        null,
                                        null,
                                        null,
                                        null);

                        Set<ConstraintViolation<TransferRequest>> violations = validator.validate(request);

                        assertThat(violations).hasSize(4);
                        assertThat(violations)
                                        .extracting(ConstraintViolation::getMessage)
                                        .containsExactlyInAnyOrder(
                                                        "Source account ID is required",
                                                        "Destination account ID is required",
                                                        "Transfer amount is required",
                                                        "Idempotency key is required");
                }
        }

        @Nested
        @DisplayName("Amount Validation Tests")
        class AmountValidationTests {

                @Test
                @DisplayName("Should fail validation when amount is zero")
                void zeroAmountFails() {
                        TransferRequest request = new TransferRequest(
                                        1L,
                                        2L,
                                        BigDecimal.ZERO,
                                        "unique-key-123");

                        Set<ConstraintViolation<TransferRequest>> violations = validator.validate(request);

                        assertThat(violations)
                                        .hasSize(1)
                                        .extracting(ConstraintViolation::getMessage)
                                        .containsExactly("Transfer amount must be at least 0.01");
                }

                @Test
                @DisplayName("Should fail validation when amount is negative")
                void negativeAmountFails() {
                        TransferRequest request = new TransferRequest(
                                        1L,
                                        2L,
                                        new BigDecimal("-100.00"),
                                        "unique-key-123");

                        Set<ConstraintViolation<TransferRequest>> violations = validator.validate(request);

                        assertThat(violations)
                                        .hasSize(1)
                                        .extracting(ConstraintViolation::getMessage)
                                        .containsExactly("Transfer amount must be at least 0.01");
                }

                @Test
                @DisplayName("Should fail validation when amount is less than 0.01")
                void amountBelowMinimumFails() {
                        TransferRequest request = new TransferRequest(
                                        1L,
                                        2L,
                                        new BigDecimal("0.009"),
                                        "unique-key-123");

                        Set<ConstraintViolation<TransferRequest>> violations = validator.validate(request);

                        assertThat(violations)
                                        .hasSize(1)
                                        .extracting(ConstraintViolation::getMessage)
                                        .containsExactly("Transfer amount must be at least 0.01");
                }
        }

        @Nested
        @DisplayName("Same Account Validation Tests")
        class SameAccountValidationTests {

                @Test
                @DisplayName("Should throw exception when source and destination accounts are the same")
                void sameSourceAndDestinationFails() {
                        assertThatThrownBy(() -> new TransferRequest(
                                        1L,
                                        1L,
                                        new BigDecimal("100.00"),
                                        "unique-key-123")).isInstanceOf(IllegalArgumentException.class)
                                        .hasMessage("Source and destination accounts must be different");
                }
        }

        @Nested
        @DisplayName("Factory Method Tests")
        class FactoryMethodTests {

                @Test
                @DisplayName("Should create request using factory method with auto-generated idempotency key")
                void createWithFactoryMethod() {
                        TransferRequest request = TransferRequest.of(1L, 2L, new BigDecimal("100.00"));

                        assertThat(request.fromAccountId()).isEqualTo(1L);
                        assertThat(request.toAccountId()).isEqualTo(2L);
                        assertThat(request.amount()).isEqualTo(new BigDecimal("100.00"));
                        assertThat(request.idempotencyKey()).isNotNull();
                        assertThat(request.idempotencyKey()).isNotEmpty();
                }
        }

        @Nested
        @DisplayName("Record Behavior Tests")
        class RecordBehaviorTests {

                @Test
                @DisplayName("Should generate correct equals and hashCode")
                void equalsAndHashCode() {
                        TransferRequest request1 = new TransferRequest(1L, 2L,
                                        new BigDecimal("100.00"), "key-123");
                        TransferRequest request2 = new TransferRequest(1L, 2L,
                                        new BigDecimal("100.00"), "key-123");

                        assertThat(request1).isEqualTo(request2);
                        assertThat(request1.hashCode()).isEqualTo(request2.hashCode());
                }

                @Test
                @DisplayName("Should generate meaningful toString")
                void toStringMethod() {
                        TransferRequest request = new TransferRequest(1L, 2L,
                                        new BigDecimal("100.00"), "key-123");

                        String toString = request.toString();
                        assertThat(toString).contains("1");
                        assertThat(toString).contains("2");
                        assertThat(toString).contains("100.00");
                        assertThat(toString).contains("key-123");
                }
        }
}
