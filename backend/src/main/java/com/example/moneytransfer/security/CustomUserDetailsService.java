package com.example.moneytransfer.security;

import com.example.moneytransfer.domain.Account;
import com.example.moneytransfer.repository.AccountRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final AccountRepository accountRepository;

    public CustomUserDetailsService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Account account = accountRepository.findByHolderName(username)
                .orElseThrow(() -> new UsernameNotFoundException("Account not found with username: " + username));

        if (!account.isActive()) {
            throw new org.springframework.security.authentication.DisabledException(
                    "Account is not active: " + username);
        }

        return new User(
                String.valueOf(account.getId()), // User Principal is still the ID for internal logic convenience
                account.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
    }

    // Load user by ID for JWT token validation
    public UserDetails loadUserById(Long id) throws UsernameNotFoundException {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("Account not found with ID: " + id));

        if (!account.isActive()) {
            throw new org.springframework.security.authentication.DisabledException("Account is not active");
        }

        return new User(
                String.valueOf(account.getId()),
                account.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
    }
}
