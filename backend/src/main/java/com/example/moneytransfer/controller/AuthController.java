package com.example.moneytransfer.controller;

import com.example.moneytransfer.dto.JwtResponse;
import com.example.moneytransfer.dto.LoginRequest;
import com.example.moneytransfer.security.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final com.example.moneytransfer.service.AccountService accountService;

    public AuthController(AuthenticationManager authenticationManager, JwtUtil jwtUtil,
            com.example.moneytransfer.service.AccountService accountService) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.accountService = accountService;
    }

    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getAccountId(), loginRequest.getPassword()));

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String jwt = jwtUtil.generateToken(userDetails);

        com.example.moneytransfer.domain.Account account = accountService
                .getAccount(Long.parseLong(userDetails.getUsername()));

        return ResponseEntity.ok(new JwtResponse(jwt, account.getId(), account.getHolderName()));
    }
}
