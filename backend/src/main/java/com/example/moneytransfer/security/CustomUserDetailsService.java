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
        try {
            Long accountId = Long.parseLong(username);
            Account account = accountRepository.findById(accountId)
                    .orElseThrow(() -> new UsernameNotFoundException("Account not found with ID: " + username));

            if (!account.isActive()) {
                throw new UsernameNotFoundException("Account is not active: " + username);
            }

            return new User(
                    String.valueOf(account.getId()),
                    account.getPassword(),
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
        } catch (NumberFormatException e) {
            throw new UsernameNotFoundException("Invalid Account ID format: " + username);
        }
    }
}
