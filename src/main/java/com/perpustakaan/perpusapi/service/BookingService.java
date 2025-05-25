package com.perpustakaan.perpusapi.service;

import com.perpustakaan.perpusapi.model.Book;
import com.perpustakaan.perpusapi.model.BookingRequest;
import com.perpustakaan.perpusapi.repo.BookingRepo;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

public class BookingService {

    private final BookingRepo bookingRepo;

    public BookingService(Connection conn) {
        this.bookingRepo = new BookingRepo(conn);
    }

    public void bookBuku(BookingRequest req) throws SQLException {
        if (!bookingRepo.isAvailable(req.bukuId)) {
            throw new SQLException("Buku tidak tersedia untuk dibooking");
        }

        int memberId = bookingRepo.getMemberIdByAccountId(req.accountId);
        int bukuDetailId = bookingRepo.findAvailableBukuDetail(req.bukuId);

        bookingRepo.insertBooking(memberId, bukuDetailId, new Timestamp(req.expiredDate.getTime()));
        bookingRepo.updateBukuDetailStatus(bukuDetailId);
        bookingRepo.decreaseJumlahTersedia(req.bukuId);
        bookingRepo.updateStatusBookingIfNeeded(req.bukuId);
    }

    public List<Book> getBookedBooksByAccount(int accountId) throws SQLException {
        return bookingRepo.getBookingByAccountId(accountId);
    }

}
