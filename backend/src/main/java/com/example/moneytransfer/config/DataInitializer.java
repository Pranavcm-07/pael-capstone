package com.example.moneytransfer.config;

import com.example.moneytransfer.domain.Account;
import com.example.moneytransfer.enums.AccountStatus;
import com.example.moneytransfer.repository.AccountRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initData(AccountRepository accountRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            // Seed data if no accounts exist
            if (accountRepository.count() == 0) {
                Account account1 = new Account(
                        null,
                        "Pranav",
                        new BigDecimal("1000.00"),
                        passwordEncoder.encode("pranav123"),
                        AccountStatus.ACTIVE);

                Account account2 = new Account(
                        null,
                        "Pranesh",
                        new BigDecimal("1000.00"),
                        passwordEncoder.encode("pranesh123"),
                        AccountStatus.ACTIVE);
                Account account3 = new Account(
                        null,
                        "Pradeep",
                        new BigDecimal("1000.00"),
                        passwordEncoder.encode("pradeep123"),
                        AccountStatus.ACTIVE);
                Account account4 = new Account(
                        null,
                        "Nivedita",
                        new BigDecimal("1000.00"),
                        passwordEncoder.encode("nivedita123"),
                        AccountStatus.ACTIVE);

                accountRepository.save(account1);
                accountRepository.save(account2);
                accountRepository.save(account3);
                accountRepository.save(account4);

                System.out.println("Data Initialized: Account 1 (pranav123) and Account 2 (pranesh123) created.");
            }
        };
    }
}
