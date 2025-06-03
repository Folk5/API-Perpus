package com.perpustakaan.perpusapi.service;

import com.perpustakaan.perpusapi.config.DatabaseConfig; // Import DatabaseConfig
import com.perpustakaan.perpusapi.model.Book;
import com.perpustakaan.perpusapi.repo.BookRepo;

import java.sql.Connection; // Import Connection
import java.sql.SQLException; // Import SQLException
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
        return bookRepository.findById(id);
    }

    public boolean addBook(Book book) {
        // Set default values
        book.setJmlTersedia(book.getJumlah());
        book.setStatusBooking(false);

        Connection conn = null;
        try {
            conn = DatabaseConfig.getConnection(); // Dapatkan koneksi
            conn.setAutoCommit(false); // Mulai transaksi

            // 1. Insert ke tabel 'buku' dan dapatkan ID nya
            int generatedBukuId = bookRepository.insert(book, conn);
            if (generatedBukuId == -1) {
                throw new SQLException("Gagal menyimpan buku, tidak mendapatkan ID.");
            }
            book.setBukuId(generatedBukuId); // Set ID buku yang di-generate ke objek

            // 2. Insert ke tabel 'bukudetails' sejumlah 'jumlah'
            if (book.getJumlah() > 0) {
                bookRepository.insertBukuDetails(generatedBukuId, book.getJumlah(), conn);
            }

            conn.commit(); // Commit transaksi jika semua berhasil
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback(); // Rollback jika ada kesalahan
                } catch (SQLException ex) {
                    ex.printStackTrace();
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

    public boolean updateBook(Book book) {
        try {
            Book existingBook = bookRepository.findById(book.getBukuId());
            if (existingBook != null) {
                bookRepository.update(book);
                return true;
            } else {
                System.out.println("Book with ID " + book.getBukuId() + " not found!");
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteBook(int id) {
        try {
            Book existingBook = bookRepository.findById(id);
            if (existingBook != null) {
                bookRepository.delete(id);
                return true;
            } else {
                System.out.println("Book with ID " + id + " not found!");
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}