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
        return bookRepository.findById(id);
    }

    public boolean addBook(Book book) {
        // ... (kode addBook tetap sama seperti sebelumnya) ...
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

    public boolean updateBook(Book bookDataToUpdate) {
        Connection conn = null;
        try {
            conn = DatabaseConfig.getConnection();
            conn.setAutoCommit(false);

            // 1. Dapatkan data buku yang ada saat ini dari database
            Book existingBook = bookRepository.findById(bookDataToUpdate.getBukuId(), conn);
            if (existingBook == null) {
                System.out.println("Book with ID " + bookDataToUpdate.getBukuId() + " not found for update!");
                conn.rollback(); // Tidak perlu rollback jika belum ada operasi, tapi untuk konsistensi
                return false;
            }

            int oldJumlahTotal = existingBook.getJumlah();
            int newJumlahTotal = bookDataToUpdate.getJumlah();

            // Sinkronisasi jml_tersedia:
            // Jika jumlah total buku diubah, jml_tersedia perlu disesuaikan.
            // Asumsi: Jika jumlah total berkurang, pengurangan diprioritaskan dari yang tersedia.
            // Jika jumlah total bertambah, penambahan masuk ke yang tersedia.
            int oldJmlTersedia = existingBook.getJmlTersedia();
            int selisihJumlahTotal = newJumlahTotal - oldJumlahTotal;
            int newJmlTersedia = oldJmlTersedia + selisihJumlahTotal;

            // Pastikan jml_tersedia tidak negatif dan tidak melebihi jumlah total baru
            if (newJmlTersedia < 0) newJmlTersedia = 0;
            if (newJmlTersedia > newJumlahTotal) newJmlTersedia = newJumlahTotal;

            bookDataToUpdate.setJmlTersedia(newJmlTersedia);

            // Update status_booking berdasarkan jml_tersedia baru
            if (newJmlTersedia == 0 && newJumlahTotal > 0) {
                bookDataToUpdate.setStatusBooking(true);
            } else if (newJmlTersedia > 0) {
                bookDataToUpdate.setStatusBooking(false);
            } else { // newJumlahTotal == 0
                bookDataToUpdate.setStatusBooking(false); // Atau true jika 0 dianggap full booked
            }


            // 2. Update tabel 'buku'
            boolean updated = bookRepository.update(bookDataToUpdate, conn);
            if (!updated) {
                conn.rollback();
                return false; // Gagal update tabel buku
            }

            // 3. Sesuaikan entri di 'bukudetails'
            if (newJumlahTotal < oldJumlahTotal) {
                // Kurangi entri di bukudetails
                int jumlahDihapus = oldJumlahTotal - newJumlahTotal;
                // Hapus dari yang tersedia (status=1) terlebih dahulu
                bookRepository.deleteAvailableBukuDetails(bookDataToUpdate.getBukuId(), jumlahDihapus, conn);
                // Catatan: Jika jumlahDihapus > jumlah yang tersedia, metode deleteAvailableBukuDetails
                // hanya akan menghapus yang tersedia. Ini mungkin memerlukan logika tambahan
                // jika Anda ingin paksa hapus atau berikan error jika item yang dipinjam harus dihapus.
                // Untuk saat ini, kita prioritaskan menghapus yang tersedia.
            } else if (newJumlahTotal > oldJumlahTotal) {
                // Tambah entri di bukudetails
                int jumlahDitambah = newJumlahTotal - oldJumlahTotal;
                bookRepository.insertBukuDetails(bookDataToUpdate.getBukuId(), jumlahDitambah, conn);
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

    public boolean deleteBook(int id) {
        // ... (kode deleteBook tetap sama seperti sebelumnya) ...
        Connection conn = null;
        try {
            conn = DatabaseConfig.getConnection();
            Book existingBook = bookRepository.findById(id);
            if (existingBook == null) {
                System.out.println("Book with ID " + id + " not found for deletion!");
                return false;
            }

            conn.setAutoCommit(false);

            bookRepository.deleteBukuDetailsByBukuId(id, conn);
            boolean deletedFromBuku = bookRepository.delete(id, conn);

            if (!deletedFromBuku) {
                conn.rollback();
                return false;
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("SQL Error during deleteBook: " + e.getMessage());
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    System.err.println("SQL Error during rollback: " + ex.getMessage());
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
}