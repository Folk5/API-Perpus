package com.perpustakaan.perpusapi.service;

import com.perpustakaan.perpusapi.model.Account;
import com.perpustakaan.perpusapi.model.Member;
import com.perpustakaan.perpusapi.repo.AccountRepo;
import com.perpustakaan.perpusapi.repo.MemberRepo;
import com.perpustakaan.perpusapi.utils.HashUtil;

import java.time.LocalDate;
import java.util.Optional;

public class AuthService {
    private final AccountRepo accountRepo;
    private final MemberRepo memberRepo;

    public AuthService(AccountRepo accountRepo, MemberRepo memberRepo) {
        this.accountRepo = accountRepo;
        this.memberRepo = memberRepo;
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

    public Optional<Account> register(String email, String password, String namaDepan, String namaBelakang, LocalDate tanggalLahir) {
        if (accountRepo.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Email sudah terdaftar");
        }

        password = HashUtil.sha256(password);

        Account account = new Account();
        account.setEmail(email);
        account.setPassword(password);

        if (accountRepo.insert(account)) {
            // ambil account yang baru saja dibuat
            Optional<Account> insertedAcc = accountRepo.findByEmail(email);
            if (insertedAcc.isPresent()) {
                int accountId = insertedAcc.get().getUserId();

                Member member = new Member();
                member.setNama_depan(namaDepan);
                member.setNama_belakang(namaBelakang);
                member.setTanggal_lahir(tanggalLahir);
                member.setAccount_id_fk(accountId);

                if (memberRepo.insert(member)) {
                    return insertedAcc;
                }
            }
        }

        return Optional.empty();
    }
}