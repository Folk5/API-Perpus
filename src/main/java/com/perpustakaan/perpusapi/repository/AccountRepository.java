package com.perpustakaan.perpusapi.repository;

import com.perpustakaan.perpusapi.model.Account;
import java.sql.*;
import java.util.Optional;

public class AccountRepository {
    private final Connection connection;

    public AccountRepository(Connection connection) {
        this.connection = connection;
    }

    public Optional<Account> findByEmail(String email) {
        String sql = "SELECT * FROM accounts WHERE email = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapResultSetToAccount(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public boolean insert(Account account) {
        String sql = "INSERT INTO accounts (email, password) VALUES (?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, account.getEmail());
            stmt.setString(2, account.getPassword());
            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                ResultSet generatedKeys = stmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    account.setUser_id(generatedKeys.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private Account mapResultSetToAccount(ResultSet rs) throws SQLException {
        return new Account(
                rs.getInt("user_id"),
                rs.getString("email"),
                rs.getString("password")
        );
    }
}
