package com.perpustakaan.perpusapi.service;

import com.perpustakaan.perpusapi.config.DatabaseConfig;
import com.perpustakaan.perpusapi.repo.AccountRepo;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class AccountService {
    public enum Role{
        ADMIN, MEMBER, NONE
    }

    public static Role login(String email, String password) {
        String sql = "SELECT user_id FROM account WHERE email = ? AND password = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            stmt.setString(2, password);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int userId = rs.getInt("user_id");

                    // Cek apakah akun ini admin
                    if (isAdmin(userId, conn)) return Role.ADMIN;

                    // Cek apakah akun ini member
                    if (isMember(userId, conn)) return Role.MEMBER;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Role.NONE;
    }

    private static boolean isAdmin(int userId, Connection conn){
        try (PreparedStatement stmt = conn.prepareStatement("SELECT admin_id FROM admin WHERE account_id_fk = ?")) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static boolean isMember(int userId, Connection conn){
        try(PreparedStatement stmt = conn.prepareStatement("SELECT user_id FROM member WHERE account_id_fk = ?")){
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public static int getAccountId(String email) {
        return AccountRepo.getAccountId(email);
    }
}
