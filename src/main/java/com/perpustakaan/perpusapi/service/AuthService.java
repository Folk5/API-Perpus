package com.perpustakaan.perpusapi.service;

import com.perpustakaan.perpusapi.model.Account;
import com.perpustakaan.perpusapi.repo.AccountRepo;
import com.perpustakaan.perpusapi.utils.HashUtil;

import java.util.Optional;

public class AuthService {
    private final AccountRepo accountRepo;

    public AuthService(AccountRepo accountRepo) {
        this.accountRepo = accountRepo;
    }

    public boolean login(String email, String password) {
        password = HashUtil.sha256(password);

        Optional<Account> accountOpt = accountRepo.findByEmail(email);
        if (accountOpt.isPresent()) {
            Account account = accountOpt.get();
            return account.getPassword().equals(password);
        }

        return false;
    }

    public Optional<Account> register(String email, String password) {
        if (accountRepo.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Email sudah terdaftar");
        }
        password = HashUtil.sha256(password);


        Account account = new Account();
        account.setEmail(email);
        account.setPassword(password);

        if (accountRepo.insert(account)) {
            if (accountRepo.findByEmail(email).isPresent()) {
                return accountRepo.findByEmail(email);
            }else{
                return Optional.empty();
            }
        }

        return Optional.empty();

    }
}