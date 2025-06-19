package com.perpustakaan.perpusapi.repo;

import com.perpustakaan.perpusapi.config.DatabaseConfig;
import com.perpustakaan.perpusapi.model.Book;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BookRepo {

    private Connection connect() throws SQLException {
        return DatabaseConfig.getConnection();
    }

    // getAll(), findById(int id), mapResultSetToBook() tetap sama seperti sebelumnya...
    public List<Book> getAll() {
        List<Book> books = new ArrayList<>();
        String sql = "SELECT * FROM buku";

        try (Connection conn = connect();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                books.add(mapResultSetToBook(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return books;
    }

    public List<Book> findAvailableBooksLimit5() {
        List<Book> books = new ArrayList<>();
        // Query ini mengambil 5 buku yang status_booking = 0 (false) dan jumlahnya lebih dari 0
        String sql = "SELECT * FROM buku WHERE status_booking = 0 AND jml_tersedia > 0 LIMIT 5";

        try (Connection conn = connect();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                books.add(mapResultSetToBook(rs));
            }
        } catch (SQLException e) {
            // Sebaiknya log error di sini atau lempar custom exception
            e.printStackTrace();
        }
        return books;
    }

    public Book findById(int id, Connection conn) throws SQLException {
        String sql = "SELECT * FROM buku WHERE buku_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToBook(rs);
                }
            }
        }
        return null;
    }

    public Book findById(int id) { // Overload untuk penggunaan non-transaksional singkat
        String sql = "SELECT * FROM buku WHERE buku_id = ?";
        try (Connection conn = connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToBook(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }


    public int insert(Book book, Connection conn) throws SQLException {
        String sql = "INSERT INTO buku (nama_buku, tipe_buku, jenis_buku, tgl_terbit, author, rakbuku_id_fk, status_booking, jumlah, jml_tersedia) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, book.getNamaBuku());
            stmt.setString(2, book.getTipeBuku());
            stmt.setString(3, book.getJenisBuku());
            stmt.setString(4, book.getTglTerbit());
            stmt.setString(5, book.getAuthor());
            stmt.setInt(6, book.getRakbukuIdFk());
            stmt.setBoolean(7, book.isStatusBooking());
            stmt.setInt(8, book.getJumlah());
            stmt.setInt(9, book.getJmlTersedia());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return generatedKeys.getInt(1);
                    }
                }
            }
        }
        return -1;
    }

    public void insertBukuDetails(int bukuIdFk, int jumlahBuku, Connection conn) throws SQLException {
        String sql = "INSERT INTO bukudetails (status, keluhan, buku_id_fk) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < jumlahBuku; i++) {
                stmt.setInt(1, 1);
                stmt.setString(2, "Tidak ada");
                stmt.setInt(3, bukuIdFk);
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }

    public boolean update(Book book, Connection conn) throws SQLException {
        String sql = "UPDATE buku SET nama_buku = ?, tipe_buku = ?, jenis_buku = ?, tgl_terbit = ?, author = ?, rakbuku_id_fk = ?, status_booking = ?, jumlah = ?, jml_tersedia = ? " +
                "WHERE buku_id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, book.getNamaBuku());
            stmt.setString(2, book.getTipeBuku());
            stmt.setString(3, book.getJenisBuku());
            stmt.setString(4, book.getTglTerbit());
            stmt.setString(5, book.getAuthor());
            stmt.setInt(6, book.getRakbukuIdFk());
            stmt.setBoolean(7, book.isStatusBooking());
            stmt.setInt(8, book.getJumlah());
            stmt.setInt(9, book.getJmlTersedia());
            stmt.setInt(10, book.getBukuId());

            return stmt.executeUpdate() > 0;
        }
    }

    public int countBukuDetailsByStatus(int bukuIdFk, int status, Connection conn) throws SQLException {
        String sql = "SELECT COUNT(*) FROM bukudetails WHERE buku_id_fk = ? AND status = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, bukuIdFk);
            stmt.setInt(2, status);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    public void deleteAvailableBukuDetails(int bukuIdFk, int jumlahDihapus, Connection conn) throws SQLException {
        String selectIdsSql = "SELECT id FROM bukudetails WHERE buku_id_fk = ? AND status = 1 LIMIT ?";
        List<Integer> idsToDelete = new ArrayList<>();
        try (PreparedStatement stmtSelect = conn.prepareStatement(selectIdsSql)) {
            stmtSelect.setInt(1, bukuIdFk);
            stmtSelect.setInt(2, jumlahDihapus);
            ResultSet rs = stmtSelect.executeQuery();
            while (rs.next()) {
                idsToDelete.add(rs.getInt("id"));
            }
        }

        if (!idsToDelete.isEmpty()) {
            String deleteSql = "DELETE FROM bukudetails WHERE id = ?";
            try (PreparedStatement stmtDelete = conn.prepareStatement(deleteSql)) {
                for (Integer id : idsToDelete) {
                    stmtDelete.setInt(1, id);
                    stmtDelete.addBatch();
                }
                stmtDelete.executeBatch();
            }
        }
    }


    public boolean deleteBukuDetailsByBukuId(int bukuId, Connection conn) throws SQLException {
        String sql = "DELETE FROM bukudetails WHERE buku_id_fk = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, bukuId);
            stmt.executeUpdate();
            return true;
        }
    }

    public boolean delete(int id, Connection conn) throws SQLException {
        String sql = "DELETE FROM buku WHERE buku_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    private Book mapResultSetToBook(ResultSet rs) throws SQLException {
        Book book = new Book();
        book.setBukuId(rs.getInt("buku_id"));
        book.setNamaBuku(rs.getString("nama_buku"));
        book.setTipeBuku(rs.getString("tipe_buku"));
        book.setJenisBuku(rs.getString("jenis_buku"));
        book.setTglTerbit(rs.getString("tgl_terbit"));
        book.setAuthor(rs.getString("author"));
        book.setRakbukuIdFk(rs.getInt("rakbuku_id_fk"));
        book.setStatusBooking(rs.getBoolean("status_booking"));
        book.setJumlah(rs.getInt("jumlah"));
        book.setJmlTersedia(rs.getInt("jml_tersedia"));
        return book;
    }
}