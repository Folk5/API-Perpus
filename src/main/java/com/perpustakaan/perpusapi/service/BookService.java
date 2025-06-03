package com.perpustakaan.perpusapi.service;

import com.perpustakaan.perpusapi.config.DatabaseConfig;
import com.perpustakaan.perpusapi.model.Book;
import com.perpustakaan.perpusapi.repo.BookRepo;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class BookService {

    private final BookRepo bookRepository;

    public BookService() {
        this.bookRepository = new BookRepo();
    }

    public List<Book> getAllBooks() {
        return bookRepository.getAll();
    }

    public Book getBookById(int id) {
        // Metode ini mungkin tidak perlu Connection jika hanya untuk validasi singkat
        // sebelum operasi utama. Namun, jika selalu dalam konteks transaksi, bisa dimodifikasi.
        return bookRepository.findById(id);
    }

    public boolean addBook(Book book) {
        book.setJmlTersedia(book.getJumlah());
        book.setStatusBooking(false);

        Connection conn = null;
        try {
            conn = DatabaseConfig.getConnection();
            conn.setAutoCommit(false);

            int generatedBukuId = bookRepository.insert(book, conn);
            if (generatedBukuId == -1) {
                throw new SQLException("Gagal menyimpan buku, tidak mendapatkan ID.");
            }
            // book.setBukuId(generatedBukuId); // ID sudah di-set di objek jika insert berhasil

            if (book.getJumlah() > 0) {
                bookRepository.insertBukuDetails(generatedBukuId, book.getJumlah(), conn);
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public boolean updateBook(Book book) {
        try {
            Book existingBook = bookRepository.findById(book.getBukuId()); //
            if (existingBook != null) {
                bookRepository.update(book); //
                return true;
            } else {
                System.out.println("Book with ID " + book.getBukuId() + " not found for update!");
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteBook(int id) {
        Connection conn = null;
        try {
            conn = DatabaseConfig.getConnection(); // Dapatkan koneksi

            // Opsional: Cek apakah buku ada menggunakan koneksi yang sama.
            // Book existingBook = bookRepository.findById(id, conn);
            // Jika menggunakan findById yang tidak meminta conn, itu akan membuka koneksi baru.
            // Untuk delete, lebih baik cek dulu dengan koneksi terpisah atau pastikan findById bisa pakai conn.
            Book existingBook = bookRepository.findById(id); // Menggunakan findById() yang ada
            if (existingBook == null) {
                System.out.println("Book with ID " + id + " not found for deletion!");
                return false; // Buku tidak ditemukan
            }

            conn.setAutoCommit(false); // Mulai transaksi

            // 1. Hapus dari bukudetails terlebih dahulu
            // (Jika ada foreign key dari booking ke bukudetails tanpa ON DELETE CASCADE,
            // dan ada booking aktif, penghapusan bukudetails akan gagal.
            // Ini adalah perilaku yang aman untuk mencegah penghapusan buku yang sedang dipinjam).
            bookRepository.deleteBukuDetailsByBukuId(id, conn);

            // 2. Hapus dari buku
            boolean deletedFromBuku = bookRepository.delete(id, conn);

            if (!deletedFromBuku) {
                // Seharusnya tidak terjadi jika existingBook ditemukan, tapi sebagai jaga-jaga
                conn.rollback();
                return false;
            }

            conn.commit(); // Commit transaksi jika semua berhasil
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("SQL Error during deleteBook: " + e.getMessage());
            if (conn != null) {
                try {
                    conn.rollback(); // Rollback jika ada kesalahan
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    System.err.println("SQL Error during rollback: " + ex.getMessage());
                }
            }
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true); // Kembalikan ke mode auto-commit default
                    conn.close(); // Tutup koneksi
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}