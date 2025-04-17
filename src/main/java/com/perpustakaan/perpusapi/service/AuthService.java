package com.perpustakaan.perpusapi.service;

import com.perpustakaan.perpusapi.model.Account;
import com.perpustakaan.perpusapi.repository.AccountRepository;
import com.perpustakaan.perpusapi.utils.HashUtil;
import com.perpustakaan.perpusapi.utils.TokenUtil;

import java.util.Optional;

public class AuthService {
    private final AccountRepository accountRepository;

    public AuthService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    // Login dengan Token
    public Optional<String> login(String email, String password) {
        password = HashUtil.sha256(password);

        Optional<Account> accountOpt = accountRepository.findByEmail(email);
        if (accountOpt.isPresent()) {
            Account account = accountOpt.get();
            if (account.getPassword().equals(password)) {
                return Optional.of(TokenUtil.generateToken(email)); // Kembalikan Token
            }
        }
        return Optional.empty();
    }

    // Register dengan Token
    public Optional<String> register(String email, String password) {
        if (accountRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Email sudah terdaftar");
        }
        password = HashUtil.sha256(password);

        Account account = new Account();
        account.setEmail(email);
        account.setPassword(password);

        if (accountRepository.insert(account)) {
            return Optional.of(TokenUtil.generateToken(email)); // Kembalikan Token
        }

        return Optional.empty();
    }
}
