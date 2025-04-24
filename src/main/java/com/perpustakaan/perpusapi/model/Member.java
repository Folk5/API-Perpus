package com.perpustakaan.perpusapi.model;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;

public class Member extends Account{
    private int member_id;
    private String nama_depan;
    private String nama_belakang;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate tanggal_lahir;
    private int account_id_fk;

    public Member(int member_id, String nama_depan, String nama_belakang, LocalDate tanggal_lahir, int account_id_fk) {
        this.member_id = member_id;
        this.nama_depan = nama_depan;
        this.nama_belakang = nama_belakang;
        this.tanggal_lahir = tanggal_lahir;
        this.account_id_fk = account_id_fk;
    }


    public int getMember_id() {
        return member_id;
    }

    public void setMember_id(int member_id) {
        this.member_id = member_id;
    }

    public int getAccount_id_fk() {
        return account_id_fk;
    }

    public void setAccount_id_fk(int account_id_fk) {
        this.account_id_fk = account_id_fk;
    }

    public String getNama_depan() {
        return nama_depan;
    }

    public void setNama_depan(String nama_depan) {
        this.nama_depan = nama_depan;
    }

    public String getNama_belakang() {
        return nama_belakang;
    }

    public void setNama_belakang(String nama_belakang) {
        this.nama_belakang = nama_belakang;
    }

    public LocalDate getTanggal_lahir() {
        return tanggal_lahir;
    }

    public void setTanggal_lahir(LocalDate tanggal_lahir) {
        this.tanggal_lahir = tanggal_lahir;
    }
}