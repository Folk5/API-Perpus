package com.perpustakaan.perpusapi.resource;

import com.perpustakaan.perpusapi.model.Account; // Import Account
import com.perpustakaan.perpusapi.model.Book;
import com.perpustakaan.perpusapi.repo.AccountRepo; // Import AccountRepo
import com.perpustakaan.perpusapi.repo.MemberRepo; // Import MemberRepo (jika dibutuhkan AuthService)
import com.perpustakaan.perpusapi.service.AuthService; // Import AuthService
import com.perpustakaan.perpusapi.service.BookService;
import com.perpustakaan.perpusapi.utils.TokenUtil; // Import TokenUtil
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context; // Import Context
import jakarta.ws.rs.core.HttpHeaders; // Import HttpHeaders
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
// import jakarta.ws.rs.core.SecurityContext; // Alternatif jika menggunakan SecurityContext

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Path("/books")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class BookResource {

    private final BookService bookService = new BookService();
    private final AccountRepo accountRepo = new AccountRepo(); // Tambahkan AccountRepo
    private final MemberRepo memberRepo = new MemberRepo(); // AuthService membutuhkannya
    private final AuthService authService = new AuthService(accountRepo, memberRepo); // Tambahkan AuthService

    @GET
    public Response getAllBooks() {
        List<Book> books = bookService.getAllBooks();
        return Response.ok(books).build();
    }

    @GET
    @Path("/{id}")
    public Response getBookById(@PathParam("id") int id) {
        Book book = bookService.getBookById(id);
        if (book != null) {
            return Response.ok(book).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Collections.singletonMap("message","Book not found"))
                    .build();
        }
    }

    @POST
    // @AuthRequired // Jika Anda ingin semua user terotentikasi bisa mencoba, lalu difilter role di bawah
    public Response createBook(Book book, @Context HttpHeaders headers) {
        // 1. Ambil token dari header
        String authHeader = headers.getHeaderString("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(Collections.singletonMap("message", "Token tidak ditemukan atau salah format!"))
                    .build();
        }
        String token = authHeader.substring("Bearer ".length());

        // 2. Verifikasi token dan ambil email
        if (!TokenUtil.verifyToken(token)) { //
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(Collections.singletonMap("message", "Token tidak valid atau sudah expired!"))
                    .build();
        }
        String email = TokenUtil.getEmailFromToken(token); //
        if (email == null) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(Collections.singletonMap("message", "Gagal mendapatkan email dari token!"))
                    .build();
        }

        // 3. Dapatkan informasi akun dan role
        Optional<Account> accountOpt = accountRepo.findByEmail(email); //
        if (accountOpt.isEmpty()) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(Collections.singletonMap("message", "Akun tidak ditemukan!"))
                    .build();
        }
        Account account = accountOpt.get();
        String role = authService.getRoleByAccountId(account.getUserId()); //

        // 4. Cek apakah role adalah admin
        if (!"admin".equalsIgnoreCase(role)) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity(Collections.singletonMap("message", "Hanya admin yang dapat menambahkan buku!"))
                    .build();
        }

        // 5. Jika admin, lanjutkan proses tambah buku
        boolean success = bookService.addBook(book); //
        if (success) {
            return Response.status(Response.Status.CREATED)
                    .entity(Collections.singletonMap("message","Book created successfully"))
                    .build();
        } else {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Collections.singletonMap("message","Failed to create book"))
                    .build();
        }
    }

    @PUT
    @Path("/{id}")
    // Tambahkan pemeriksaan role admin jika diperlukan, serupa dengan POST
    public Response updateBook(@PathParam("id") int id, Book book, @Context HttpHeaders headers) {
        // Implementasi pemeriksaan role admin (mirip dengan createBook)
        String authHeader = headers.getHeaderString("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) { return Response.status(Response.Status.UNAUTHORIZED).entity(Collections.singletonMap("message", "Token tidak ditemukan")).build(); }
        String token = authHeader.substring("Bearer ".length());
        if (!TokenUtil.verifyToken(token)) { return Response.status(Response.Status.UNAUTHORIZED).entity(Collections.singletonMap("message", "Token tidak valid")).build(); }
        String email = TokenUtil.getEmailFromToken(token);
        Optional<Account> accountOpt = accountRepo.findByEmail(email);
        if (accountOpt.isEmpty()) { return Response.status(Response.Status.UNAUTHORIZED).entity(Collections.singletonMap("message", "Akun tidak ditemukan")).build(); }
        String role = authService.getRoleByAccountId(accountOpt.get().getUserId());
        if (!"admin".equalsIgnoreCase(role)) { return Response.status(Response.Status.FORBIDDEN).entity(Collections.singletonMap("message", "Hanya admin yang dapat mengubah buku")).build(); }

        book.setBukuId(id);
        boolean success = bookService.updateBook(book);
        if (success) {
            return Response.ok(Collections.singletonMap("message","Book updated successfully")).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND) // Atau BAD_REQUEST tergantung kasus
                    .entity(Collections.singletonMap("message","Failed to update book"))
                    .build();
        }
    }

    @DELETE
    @Path("/{id}")
    // Tambahkan pemeriksaan role admin jika diperlukan, serupa dengan POST
    public Response deleteBook(@PathParam("id") int id, @Context HttpHeaders headers) {
        // Implementasi pemeriksaan role admin (mirip dengan createBook)
        String authHeader = headers.getHeaderString("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) { return Response.status(Response.Status.UNAUTHORIZED).entity(Collections.singletonMap("message", "Token tidak ditemukan")).build(); }
        String token = authHeader.substring("Bearer ".length());
        if (!TokenUtil.verifyToken(token)) { return Response.status(Response.Status.UNAUTHORIZED).entity(Collections.singletonMap("message", "Token tidak valid")).build(); }
        String email = TokenUtil.getEmailFromToken(token);
        Optional<Account> accountOpt = accountRepo.findByEmail(email);
        if (accountOpt.isEmpty()) { return Response.status(Response.Status.UNAUTHORIZED).entity(Collections.singletonMap("message", "Akun tidak ditemukan")).build(); }
        String role = authService.getRoleByAccountId(accountOpt.get().getUserId());
        if (!"admin".equalsIgnoreCase(role)) { return Response.status(Response.Status.FORBIDDEN).entity(Collections.singletonMap("message", "Hanya admin yang dapat menghapus buku")).build(); }


        boolean success = bookService.deleteBook(id);
        if (success) {
            return Response.ok(Collections.singletonMap("message","Book deleted successfully")).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND) // Atau BAD_REQUEST
                    .entity(Collections.singletonMap("message","Failed to delete book"))
                    .build();
        }
    }
}