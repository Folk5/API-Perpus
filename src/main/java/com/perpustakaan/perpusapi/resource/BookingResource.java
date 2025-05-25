package com.perpustakaan.perpusapi.resource;

import com.perpustakaan.perpusapi.config.DatabaseConfig;
import com.perpustakaan.perpusapi.model.Book;
import com.perpustakaan.perpusapi.model.BookingRequest;
import com.perpustakaan.perpusapi.service.BookingService;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Path("/booking")
public class BookingResource {

    // Endpoint untuk booking buku
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response bookingBuku(BookingRequest req) {
        try (Connection conn = DatabaseConfig.getConnection()) {
            BookingService service = new BookingService(conn);
            service.bookBuku(req);
            return Response.ok("{\"message\": \"Booking berhasil!\"}").build();
        } catch (SQLException e) {
            e.printStackTrace();
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"message\": \"" + e.getMessage() + "\"}")
                    .build();
        }
    }

    // Endpoint untuk mendapatkan daftar buku yang sedang dipinjam oleh user berdasarkan accountId
    @GET
    @Path("/bookings/{accountId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getBookingsByAccountId(@PathParam("accountId") int accountId) {
        // List untuk menampung data buku yang dipinjam
        List<Book> books = new ArrayList<>();

        // Query yang akan digunakan untuk mengambil data booking berdasarkan accountId
        String sql = """
    SELECT b.buku_id, b.nama_buku, b.tipe_buku, b.jenis_buku, b.tgl_terbit, b.author,
           b.rakbuku_id_fk, b.status_booking, b.jumlah,
           (
               SELECT COUNT(*) FROM bukudetails bd2
               WHERE bd2.buku_id_fk = b.buku_id AND bd2.status = 1
           ) AS jml_tersedia
    FROM booking bk
    JOIN bukudetails bd ON bk.bukuDetails_id_fk = bd.id  -- Perbaikan di sini
    JOIN buku b ON bd.buku_id_fk = b.buku_id
    JOIN member m ON bk.member_id_fk = m.member_id
    WHERE m.account_id_fk = ? AND bd.status = 0
    """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            // Set parameter accountId
            stmt.setInt(1, accountId);
            ResultSet rs = stmt.executeQuery();

            // Ambil hasil query dan simpan ke dalam list
            while (rs.next()) {
                Book book = new Book(
                        rs.getInt("buku_id"),
                        rs.getString("nama_buku"),
                        rs.getString("tipe_buku"),
                        rs.getString("jenis_buku"),
                        rs.getString("tgl_terbit"),
                        rs.getString("author"),
                        rs.getInt("rakbuku_id_fk"),
                        rs.getBoolean("status_booking"),
                        rs.getInt("jumlah"),
                        rs.getInt("jml_tersedia")
                );
                books.add(book);
            }

            // Jika data ditemukan, kembalikan response dengan data
            if (!books.isEmpty()) {
                return Response.ok(books).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(Collections.singletonMap("message", "Tidak ada data booking ditemukan"))
                        .build();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Jika terjadi kesalahan dalam query atau koneksi, kembalikan error response
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Collections.singletonMap("message", "Gagal mengambil data booking: " + e.getMessage()))
                    .build();
        }
    }

}
