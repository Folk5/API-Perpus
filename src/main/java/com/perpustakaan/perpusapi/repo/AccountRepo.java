package com.perpustakaan.perpusapi.repo;

import com.perpustakaan.perpusapi.config.DatabaseConfig;
import com.perpustakaan.perpusapi.model.Account;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.AbstractCollection;
import java.util.Optional;

public class AccountRepo {

    public static int getAccountId(String email) {
        String sql = "SELECT user_id FROM account WHERE email = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("user_id");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return -1; // tidak ditemukan
    }

    // (opsional) bisa ditambah ini kalau kamu mau pakai versi check login di repo
    public static boolean checkAccount(String email, String password) {
        String sql = "SELECT 1 FROM account WHERE email = ? AND password = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            stmt.setString(2, password);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public Optional<Account> getAccountModelById(int id){
        String sql = "SELECT * FROM account WHERE user_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Account user = new Account(
                            rs.getInt("user_id"),
                            rs.getString("username"),
                            rs.getString("password")
                    );
                    return Optional.of(user);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return Optional.empty(); // tidak ditemukan
    }
}
