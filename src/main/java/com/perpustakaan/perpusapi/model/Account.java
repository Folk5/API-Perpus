package com.perpustakaan.perpusapi.model;

import java.sql.ResultSet;
import java.sql.SQLException;

public class Account{
    private int user_id;
    private String email;
    private String password;

    public Account() {
    }

    public Account(int user_id, String email, String password){
        this.user_id = user_id;
        this.email = email;
        this.password = password;
    }


    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}