package com.perpustakaan.perpusapi.service;
//
//import com.perpustakaan.perpusapi.model.Account;
//import com.perpustakaan.perpusapi.repo.AuthRepo;
//
//public class AuthService {
//
//    private AuthRepo authRepo = new AuthRepo();
//
//    public Account login(String email, String password) {
//        Account acc = authRepo.getAccountByEmail(email);
//        if (acc != null && acc.getPassword().equals(password)) {
//            return acc;
//        }
//        return null;
//    }
//}

import com.perpustakaan.perpusapi.model.Account;
import com.perpustakaan.perpusapi.repo.AccountRepo;
import com.perpustakaan.perpusapi.utils.HashUtil;

import java.util.Optional;

public class AuthService {
    private final AccountRepo usersRepository;

    public AuthService(AccountRepo usersRepository) {
        this.usersRepository = usersRepository;
    }

    public boolean login(String email, String password) {
        password = HashUtil.sha256(password);
        System.out.println("Input Password: " + password);
        int usersOpt = usersRepository.getAccountId(email);
        if (usersOpt!=-1) {
            Optional<Account> acc = usersRepository.getAccountModelById(usersOpt);
//            Users users = usersOpt.getAcc();
//            System.out.println("User Password: " + users.getPassword());
            return acc.get().getPassword().equals(password);
        }

        return false;
    }
}