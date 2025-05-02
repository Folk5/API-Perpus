package com.perpustakaan.perpusapi.service;

import com.perpustakaan.perpusapi.model.Member;
import com.perpustakaan.perpusapi.repo.MemberRepo;

import javax.swing.text.html.Option;
import java.util.Optional;

public class MemberService {

    private final MemberRepo memberRepo=  new MemberRepo();

    public Optional<Member>getProfile(int accountId) {
        return memberRepo.findByUserId(accountId);
    }

    public boolean updateProfile(Member member) {
        return memberRepo.update(member);
    }
}
