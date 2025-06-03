package com.perpustakaan.perpusapi.repo;

import com.perpustakaan.perpusapi.model.Book;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BookingRepo {

    private final Connection conn;

    public BookingRepo(Connection conn) {
        this.conn = conn;
    }

    public int getMemberIdByAccountId(int accountId) throws SQLException {
        String sql = "SELECT member_id FROM member WHERE account_id_fk = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, accountId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt("member_id");
        }
        throw new SQLException("Member not found for accountId: " + accountId);
    }

    public int findAvailableBukuDetail(int bukuId) throws SQLException {
        String sql = "SELECT id FROM bukudetails WHERE buku_id_fk = ? AND status = 1 LIMIT 1";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, bukuId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            } else {
                throw new RuntimeException("Tidak ada buku tersedia untuk bukuId: " + bukuId);
            }
        }
    }


    public void insertBooking(int memberId, int bukuDetailsId, Timestamp expiredDate) throws SQLException {
        String sql = "INSERT INTO booking (booking_date, expired_date, member_id_fk, bukuDetails_id_fk) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
            stmt.setTimestamp(2, expiredDate);
            stmt.setInt(3, memberId);
            stmt.setInt(4, bukuDetailsId);
            stmt.executeUpdate();
        }
    }

    public void updateBukuDetailStatus(int bukuDetailsId) throws SQLException {
        String sql = "UPDATE bukudetails SET status = 0 WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, bukuDetailsId);
            stmt.executeUpdate();
        }
    }

    public void decreaseJumlahTersedia(int bukuId) throws SQLException {
        String sql = "UPDATE buku SET jml_tersedia = jml_tersedia - 1 WHERE buku_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, bukuId);
            stmt.executeUpdate();
        }
    }

    public void updateStatusBookingIfNeeded(int bukuId) throws SQLException {
        String sql = "UPDATE buku SET status_booking = 1 WHERE buku_id = ? AND jml_tersedia = 0";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, bukuId);
            stmt.executeUpdate();
        }
    }

    public boolean isAvailable(int bukuId) throws SQLException {
        String sql = "SELECT jml_tersedia FROM buku WHERE buku_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, bukuId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("jml_tersedia") > 0;
            }
        }
        return false;
    }

    public List<Book> getBookingByAccountId(int accountId) throws SQLException {
        List<Book> books = new ArrayList<>();
        String sql = """
        SELECT b.buku_id, b.nama_buku, b.tipe_buku, b.jenis_buku, b.tgl_terbit, b.author,
               b.rakbuku_id_fk, b.status_booking, b.jumlah,
               r.jenis_rak,  -- Added this column
               (
                   SELECT COUNT(*) FROM bukudetails bd2
                   WHERE bd2.buku_id_fk = b.buku_id AND bd2.status = 1
               ) AS jml_tersedia,
               bk.booking_date,  -- Added these columns explicitly
               bk.expired_date
        FROM booking bk
        JOIN bukudetails bd ON bk.bukuDetails_id_fk = bd.id
        JOIN buku b ON bd.buku_id_fk = b.buku_id
        JOIN rakbuku r ON b.rakbuku_id_fk = r.rakbuku_id  -- Added this join
        JOIN member m ON bk.member_id_fk = m.member_id
        WHERE m.account_id_fk = ? AND bd.status = 0
        """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, accountId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Book book = new Book(
                        rs.getInt("buku_id"),
                        rs.getString("nama_buku"),
                        rs.getString("tipe_buku"),
                        rs.getString("jenis_buku"),
                        rs.getString("tgl_terbit"),
                        rs.getString("author"),
                        rs.getInt("rakbuku_id_fk"),
                        rs.getString("jenis_rak"),  // Now this will work
                        rs.getBoolean("status_booking"),
                        rs.getInt("jumlah"),
                        rs.getInt("jml_tersedia"),
                        rs.getDate("booking_date"),
                        rs.getDate("expired_date")
                );
                books.add(book);
            }
        }

        return books;
    }


}