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
                        "User One",
                        new BigDecimal("1000.00"),
                        passwordEncoder.encode("pass1"),
                        AccountStatus.ACTIVE);

                Account account2 = new Account(
                        null,
                        "User Two",
                        new BigDecimal("1000.00"),
                        passwordEncoder.encode("pass2"),
                        AccountStatus.ACTIVE);

                accountRepository.save(account1);
                accountRepository.save(account2);

                System.out.println("Data Initialized: Account 1 (pass1) and Account 2 (pass2) created.");
            } else {
                // Fix existing accounts with missing passwords OR update them to match the
                // pattern (id:pass<id>)
                accountRepository.findAll().forEach(account -> {
                    String newPassword = "pass" + account.getId();
                    account.setPassword(passwordEncoder.encode(newPassword));
                    accountRepository.save(account);
                    System.out.println("Updated account " + account.getId() + " with password '" + newPassword + "'");
                });
            }
        };
    }
}
