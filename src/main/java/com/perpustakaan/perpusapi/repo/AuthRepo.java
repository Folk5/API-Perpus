package com.perpustakaan.perpusapi.repo;
//
//import com.perpustakaan.perpusapi.config.DatabaseConfig;
//import com.perpustakaan.perpusapi.model.Account;
//
//import java.sql.Connection;
//import java.sql.PreparedStatement;
//import java.sql.ResultSet;
//
//public class AuthRepo {
//
//    public Account getAccountByEmail(String email) {
//        String query = "SELECT * FROM account WHERE email = ?";
//        try (Connection conn = DatabaseConfig.getConnection();
//             PreparedStatement stmt = conn.prepareStatement(query)) {
//
//            stmt.setString(1, email);
//            ResultSet rs = stmt.executeQuery();
//
//            if (rs.next()) {
//                return new Account(
//                        rs.getInt("user_id"),
//                        rs.getString("email"),
//                        new String(rs.getBytes("password")) // karena password bertipe BLOB
//                );
//            }
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return null;
//    }
//}
//
