package com.perpustakaan.perpusapi.repo;

import com.perpustakaan.perpusapi.model.Book;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BookRepo {

    private final String URL = "jdbc:mysql://localhost:3306/perpus_pbo_2024";
    private final String USERNAME = "root";
    private final String PASSWORD = "";

    // Metode connect() tetap sama
    private Connection connect() throws SQLException {
        return DriverManager.getConnection(URL, USERNAME, PASSWORD);
    }

    public List<Book> getAll() {
        List<Book> books = new ArrayList<>();
        String sql = "SELECT * FROM buku";

        try (Connection conn = connect(); // Koneksi lokal untuk operasi read-only ini
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

    public Book findById(int id) {
        String sql = "SELECT * FROM buku WHERE buku_id = ?";
        try (Connection conn = connect(); // Koneksi lokal
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
            stmt.setString(4, book.getTglTerbit()); // Pastikan format tanggal sesuai dengan kolom DB
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
                stmt.setInt(1, 1); // status default 1 (tersedia)
                stmt.setString(2, "Tidak ada"); // keluhan default
                stmt.setInt(3, bukuIdFk);
                stmt.addBatch(); // Tambahkan ke batch
            }
            stmt.executeBatch(); // Eksekusi batch insert
        }
    }

    public void update(Book book) {
        String sql = "UPDATE buku SET nama_buku = ?, tipe_buku = ?, jenis_buku = ?, tgl_terbit = ?, author = ?, rakbuku_id_fk = ?, status_booking = ?, jumlah = ?, jml_tersedia = ? " +
                "WHERE buku_id = ?";

        try (Connection conn = connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

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

            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void delete(int id) {
        String sql = "DELETE FROM buku WHERE buku_id = ?";

        try (Connection conn = connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
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