package com.perpustakaan.perpusapi.repo;

import com.perpustakaan.perpusapi.config.DatabaseConfig;
import com.perpustakaan.perpusapi.model.Member;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class MemberRepo {
    private final String table = "member";
    public Optional<Member> findByUserId(int id){
        String sql = "SELECT * FROM "+table+" WHERE account_id_fk = ?";
        try(Connection conn = DatabaseConfig.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)){

            stmt.setInt(1,id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Member member = new Member(
                        rs.getInt("member_id"),
                        rs.getString("nama_depan"),
                        rs.getString("nama_belakang"),
                        rs.getDate("tanggal_lahir").toLocalDate(),
                        rs.getInt("account_id_fk")
                );
                return Optional.of(member);
            }

        }catch (SQLException e){
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public boolean insert(Member member) {
        String sql = "INSERT INTO member (nama_depan, nama_belakang, tanggal_lahir, account_id_fk) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, member.getNama_depan());
            stmt.setString(2, member.getNama_belakang());
            stmt.setDate(3, java.sql.Date.valueOf(member.getTanggal_lahir()));
            stmt.setInt(4, member.getAccount_id_fk());

            return stmt.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
