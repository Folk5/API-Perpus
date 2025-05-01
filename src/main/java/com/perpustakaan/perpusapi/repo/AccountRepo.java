package com.perpustakaan.perpusapi.repo;

import com.perpustakaan.perpusapi.config.DatabaseConfig;
import com.perpustakaan.perpusapi.model.Account;
import com.perpustakaan.perpusapi.model.Member;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AccountRepo {
    public Optional<Account> findById(int id) {
        String sql = "SELECT * FROM account WHERE user_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Account account = new Account(
                        rs.getInt("user_id"),
                        rs.getString("email"),
                        rs.getString("password")
                );
                return Optional.of(account);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public Optional<Account> findByEmail(String email) {
        String sql = "SELECT * FROM account WHERE email = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Account account = new Account(
                        rs.getInt("user_id"),
                        rs.getString("email"),
                        rs.getString("password")
                );
                return Optional.of(account);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public boolean insert(Account account) {
        String sql = "INSERT INTO account (email, password) VALUES (?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, account.getEmail());
            stmt.setString(2, account.getPassword());

            return stmt.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean update(Account account) {
        String sql = "UPDATE account SET email = ?, password = ? WHERE user_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, account.getEmail());
            stmt.setString(2, account.getPassword());
            stmt.setInt(3, account.getUserId());

            return stmt.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM account WHERE user_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<Account> getAll(){
        String sql = "SELECT * FROM account";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            List<Account> listAcc = new ArrayList<>();
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Account account = new Account(
                        rs.getInt("user_id"),
                        rs.getString("email"),
                        rs.getString("password")
                );
                listAcc.add(account);
            }
            return listAcc;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean insertMember(Member member) {
        String sql = "INSERT INTO member (nama_depan, nama_belakang, tanggal_lahir, id) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, member.getNama_depan());
            stmt.setString(2, member.getNama_belakang());
            stmt.setObject(3, member.getTanggal_lahir()); // untuk LocalDate
            stmt.setInt(4, member.getAccount_id_fk());

            return stmt.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

}