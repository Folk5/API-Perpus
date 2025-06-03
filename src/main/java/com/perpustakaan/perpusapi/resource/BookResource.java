package com.perpustakaan.perpusapi.resource;

import com.perpustakaan.perpusapi.model.Account;
import com.perpustakaan.perpusapi.model.Book;
import com.perpustakaan.perpusapi.repo.AccountRepo;
import com.perpustakaan.perpusapi.repo.MemberRepo;
import com.perpustakaan.perpusapi.service.AuthService;
import com.perpustakaan.perpusapi.service.BookService;
import com.perpustakaan.perpusapi.utils.TokenUtil;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Path("/books")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class BookResource {

    private final BookService bookService = new BookService();
    private final AccountRepo accountRepo = new AccountRepo();
    private final MemberRepo memberRepo = new MemberRepo();
    private final AuthService authService = new AuthService(accountRepo, memberRepo);

    @GET
    public Response getAllBooks() {
        List<Book> books = bookService.getAllBooks();
        return Response.ok(books).build();
    }

    @GET
    @Path("/{id}")
    public Response getBookById(@PathParam("id") int id) {
        Book book = bookService.getBookById(id); //
        if (book != null) {
            return Response.ok(book).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Collections.singletonMap("message","Book not found"))
                    .build();
        }
    }

    @POST
    public Response createBook(Book book, @Context HttpHeaders headers) {
        String authHeader = headers.getHeaderString("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(Collections.singletonMap("message", "Token tidak ditemukan atau salah format!"))
                    .build();
        }
        String token = authHeader.substring("Bearer ".length());

        if (!TokenUtil.verifyToken(token)) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(Collections.singletonMap("message", "Token tidak valid atau sudah expired!"))
                    .build();
        }
        String email = TokenUtil.getEmailFromToken(token);
        if (email == null) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(Collections.singletonMap("message", "Gagal mendapatkan email dari token!"))
                    .build();
        }

        Optional<Account> accountOpt = accountRepo.findByEmail(email);
        if (accountOpt.isEmpty()) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(Collections.singletonMap("message", "Akun tidak ditemukan!"))
                    .build();
        }
        Account account = accountOpt.get();
        String role = authService.getRoleByAccountId(account.getUserId());

        if (!"admin".equalsIgnoreCase(role)) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity(Collections.singletonMap("message", "Hanya admin yang dapat menambahkan buku!"))
                    .build();
        }

        boolean success = bookService.addBook(book);
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
    public Response updateBook(@PathParam("id") int id, Book book, @Context HttpHeaders headers) {
        // 1. Ambil dan validasi token & role (sama seperti di POST/DELETE)
        String authHeader = headers.getHeaderString("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(Collections.singletonMap("message", "Token tidak ditemukan atau salah format!"))
                    .build();
        }
        String token = authHeader.substring("Bearer ".length());

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

        Optional<Account> accountOpt = accountRepo.findByEmail(email); //
        if (accountOpt.isEmpty()) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(Collections.singletonMap("message", "Akun tidak ditemukan!"))
                    .build();
        }
        Account account = accountOpt.get();
        String role = authService.getRoleByAccountId(account.getUserId()); //

        // 2. Cek apakah role adalah admin
        if (!"admin".equalsIgnoreCase(role)) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity(Collections.singletonMap("message", "Hanya admin yang dapat mengubah data buku!"))
                    .build();
        }

        // 3. Jika admin, lanjutkan proses update buku
        // Pastikan ID dari path parameter diset ke objek buku yang akan diupdate
        book.setBukuId(id);

        boolean success = bookService.updateBook(book); //
        if (success) {
            return Response.ok(Collections.singletonMap("message","Book updated successfully")).build();
        } else {
            // Bisa jadi karena buku tidak ditemukan, atau ada error saat update
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Collections.singletonMap("message","Failed to update book. It might not exist or an error occurred."))
                    .build();
        }
    }

    @DELETE
    @Path("/{id}")
    public Response deleteBook(@PathParam("id") int id, @Context HttpHeaders headers) {
        String authHeader = headers.getHeaderString("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(Collections.singletonMap("message", "Token tidak ditemukan atau salah format!"))
                    .build();
        }
        String token = authHeader.substring("Bearer ".length());

        if (!TokenUtil.verifyToken(token)) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(Collections.singletonMap("message", "Token tidak valid atau sudah expired!"))
                    .build();
        }
        String email = TokenUtil.getEmailFromToken(token);
        if (email == null) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(Collections.singletonMap("message", "Gagal mendapatkan email dari token!"))
                    .build();
        }

        Optional<Account> accountOpt = accountRepo.findByEmail(email);
        if (accountOpt.isEmpty()) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(Collections.singletonMap("message", "Akun tidak ditemukan!"))
                    .build();
        }
        Account account = accountOpt.get();
        String role = authService.getRoleByAccountId(account.getUserId());

        if (!"admin".equalsIgnoreCase(role)) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity(Collections.singletonMap("message", "Hanya admin yang dapat menghapus buku!"))
                    .build();
        }

        boolean success = bookService.deleteBook(id);
        if (success) {
            return Response.ok(Collections.singletonMap("message","Book deleted successfully")).build();
        } else {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Collections.singletonMap("message","Failed to delete book. It might not exist or be in use."))
                    .build();
        }
    }
}