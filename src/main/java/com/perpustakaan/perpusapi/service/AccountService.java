package com.perpustakaan.perpusapi.service;

import com.perpustakaan.perpusapi.model.Account;
import com.perpustakaan.perpusapi.model.Member;
import com.perpustakaan.perpusapi.repo.AccountRepo;
import com.perpustakaan.perpusapi.repo.MemberRepo;

import java.util.List;
import java.util.Optional;

public class AccountService {
    private final AccountRepo accountRepo;
    private final MemberRepo memberRepository;

    public AccountService(AccountRepo accountRepo, MemberRepo memberRepository) {
        this.accountRepo = accountRepo;
        this.memberRepository = memberRepository;
    }

    public AccountService(MemberRepo memberRepo) {
        this(null, memberRepo);
    }

    public AccountService(AccountRepo accountRepo) {
        this(accountRepo, null);
    }

    public Member getProfileByEmail(String email){
        Optional<Account> accountOpt = this.accountRepo.findByEmail(email);
        if (accountOpt.isEmpty()) {
            throw new IllegalArgumentException("Email tidak ditemukan");
        }

        Account account = accountOpt.get();
        Optional<Member> memberOpt = this.memberRepository.findByUserId(account.getUserId());
        if (memberOpt.isPresent()) {
            Member member = memberOpt.get();
            member.setEmail(email);
            return (member);
        }
        return null;
    }

    public Member getProfileByUserId(int id){
        Optional<Member> memberOpt = memberRepository.findByUserId(id);
        if (memberOpt.isPresent()) {
            Member member = memberOpt.get();
            Optional<Account> acc = accountRepo.findById(id);
            member.setEmail(acc.get().getEmail());
            return member;
        }
        return null;
    }

    public List<Account> getAllAccount(){
        return this.accountRepo.getAll();
    }
}

