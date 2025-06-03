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
import java.util.*;

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

    @GET
    @Path("/bookings/{accountId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getBookingsByAccountId(@PathParam("accountId") int accountId) {
        List<Book> books = new ArrayList<>();

        String sql = """
        SELECT b.buku_id, b.nama_buku, b.tipe_buku, b.jenis_buku, b.tgl_terbit, b.author,
               b.rakbuku_id_fk, b.status_booking, b.jumlah,
               r.jenis_rak,  -- Added jenis_rak from rakbuku table
               bk.booking_date, bk.expired_date, 
               (
                   SELECT COUNT(*) FROM bukudetails bd2
                   WHERE bd2.buku_id_fk = b.buku_id AND bd2.status = 1
               ) AS jml_tersedia
        FROM booking bk
        JOIN bukudetails bd ON bk.bukuDetails_id_fk = bd.id
        JOIN buku b ON bd.buku_id_fk = b.buku_id
        JOIN rakbuku r ON b.rakbuku_id_fk = r.rakbuku_id  -- Added join to rakbuku
        JOIN member m ON bk.member_id_fk = m.member_id
        WHERE m.account_id_fk = ? AND bd.status = 0
        """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
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
                        rs.getString("jenis_rak"),  // Added this field
                        rs.getBoolean("status_booking"),
                        rs.getInt("jumlah"),
                        rs.getInt("jml_tersedia"),
                        rs.getDate("booking_date"),
                        rs.getDate("expired_date")
                );
                books.add(book);
            }

            if (!books.isEmpty()) {
                return Response.ok(books).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(Collections.singletonMap("message", "Tidak ada data booking ditemukan"))
                        .build();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Collections.singletonMap("message", "Gagal mengambil data booking: " + e.getMessage()))
                    .build();
        }
    }

    @GET
    @Path("/all-booking")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllUserBookings() {
        String sql = """
    SELECT m.member_id, m.nama_depan, m.nama_belakang,
           b.nama_buku, bk.booking_date, bk.expired_date, bk.booking_id
    FROM booking bk
    JOIN member m ON bk.member_id_fk = m.member_id
    JOIN bukudetails bd ON bk.bukuDetails_id_fk = bd.id
    JOIN buku b ON bd.buku_id_fk = b.buku_id
    WHERE bd.status = 0
    ORDER BY m.member_id, bk.booking_date DESC
    """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            ResultSet rs = stmt.executeQuery();
            Map<Integer, Map<String, Object>> grouped = new LinkedHashMap<>();

            while (rs.next()) {
                int memberId = rs.getInt("member_id");
                String fullName = rs.getString("nama_depan") + " " + rs.getString("nama_belakang");

                Map<String, Object> userGroup = grouped.computeIfAbsent(memberId, k -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("memberId", memberId);
                    m.put("nama", fullName);
                    m.put("pinjaman", new ArrayList<Map<String, Object>>());
                    return m;
                });

                List<Map<String, Object>> pinjaman = (List<Map<String, Object>>) userGroup.get("pinjaman");

                Map<String, Object> detail = new HashMap<>();
                detail.put("judul", rs.getString("nama_buku"));
                detail.put("bookingDate", rs.getDate("booking_date"));
                detail.put("expiredDate", rs.getDate("expired_date"));
                detail.put("bookingId", rs.getInt("booking_id"));

                pinjaman.add(detail);
            }

            return Response.ok(new ArrayList<>(grouped.values())).build();

        } catch (SQLException e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("message", "Gagal mengambil data booking: " + e.getMessage()))
                    .build();
        }
    }

    @POST
    @Path("/return-book/{bookingId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response kembalikanBuku(@PathParam("bookingId") int bookingId) {
        try (Connection conn = DatabaseConfig.getConnection()) {
            conn.setAutoCommit(false);

            // 1. Cari buku detail dari booking_id
            String getDetailSql = "SELECT bukuDetails_id_fk FROM booking WHERE booking_id = ?";
            int detailId = -1;

            try (PreparedStatement stmt = conn.prepareStatement(getDetailSql)) {
                stmt.setInt(1, bookingId);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    detailId = rs.getInt("bukuDetails_id_fk");
                } else {
                    throw new SQLException("Booking ID tidak ditemukan");
                }
            }

            // 2. Update bukudetails status jadi 1
            try (PreparedStatement stmt = conn.prepareStatement(
                    "UPDATE bukudetails SET status = 1 WHERE id = ?")) {
                stmt.setInt(1, detailId);
                stmt.executeUpdate();
            }

            // 3. Ambil buku_id_fk dan update jml_tersedia
            int bukuId = -1;
            try (PreparedStatement stmt = conn.prepareStatement(
                    "SELECT buku_id_fk FROM bukudetails WHERE id = ?")) {
                stmt.setInt(1, detailId);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    bukuId = rs.getInt("buku_id_fk");
                }
            }

            if (bukuId > 0) {
                try (PreparedStatement stmt = conn.prepareStatement(
                        "UPDATE buku SET jml_tersedia = jml_tersedia + 1, status_booking = 0 WHERE buku_id = ?")) {
                    stmt.setInt(1, bukuId);
                    stmt.executeUpdate();
                }
            }

            conn.commit();
            return Response.ok(Map.of("message", "Buku berhasil dikembalikan")).build();

        } catch (SQLException e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("message", "Gagal mengembalikan buku: " + e.getMessage()))
                    .build();
        }
    }

}