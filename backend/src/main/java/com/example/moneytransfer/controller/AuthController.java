package com.example.moneytransfer.controller;

import com.example.moneytransfer.config.JwtUtil;
import com.example.moneytransfer.domain.User;
import com.example.moneytransfer.dto.AuthResponse;
import com.example.moneytransfer.dto.LoginRequest;
import com.example.moneytransfer.dto.RegisterRequest;
import com.example.moneytransfer.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final com.example.moneytransfer.repository.AccountRepository accountRepository;

    @PostMapping("/register")
    @org.springframework.transaction.annotation.Transactional
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            // Return JSON
            return ResponseEntity.badRequest().body(
                    Map.of("success", false, "message", "Username already exists")
            );
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        try {
            userRepository.save(user);

            // Create default account with 1000 balance
            com.example.moneytransfer.domain.Account account = new com.example.moneytransfer.domain.Account();
            
            // Generate a random 8-digit account ID
            java.util.Random random = new java.util.Random();
            long newId;
            do {
                newId = 10000000L + random.nextInt(90000000);
            } while (accountRepository.existsById(newId));
            
            account.setId(newId);
            account.setUser(user);
            account.setHolderName(user.getUsername());
            account.setStatus(com.example.moneytransfer.domain.enums.AccountStatus.ACTIVE);
            account.setBalance(new java.math.BigDecimal("1000.00"));
            accountRepository.save(account);

            // Return JSON success
            return ResponseEntity.ok(
                    Map.of("success", true, "message", "User registered successfully")
            );
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(
                    Map.of("success", false, "message", "Registration failed: " + e.getMessage())
            );
        }
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        final UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());
        final String token = jwtUtil.generateToken(userDetails);

        return ResponseEntity.ok(new AuthResponse(token));
    }
}
