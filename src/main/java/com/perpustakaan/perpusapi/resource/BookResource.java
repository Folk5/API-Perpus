package com.perpustakaan.perpusapi.resource;

import com.perpustakaan.perpusapi.model.Book;
import com.perpustakaan.perpusapi.service.BookService;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

@Path("/books")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class BookResource {

    private final BookService bookService = new BookService();

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
                    .entity("{\"message\":\"Book not found\"}")
                    .build();
        }
    }

    @POST
    public Response createBook(Book book) {
        boolean success = bookService.addBook(book);
        if (success) {
            return Response.status(Response.Status.CREATED)
                    .entity("{\"message\":\"Book created successfully\"}")
                    .build();
        } else {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"message\":\"Failed to create book\"}")
                    .build();
        }
    }

    @PUT
    @Path("/{id}")
    public Response updateBook(@PathParam("id") int id, Book book) {
        book.setBukuId(id); // Pastikan ID yang diupdate benar
        boolean success = bookService.updateBook(book);
        if (success) {
            return Response.ok("{\"message\":\"Book updated successfully\"}").build();
        } else {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"message\":\"Failed to update book\"}")
                    .build();
        }
    }

    @DELETE
    @Path("/{id}")
    public Response deleteBook(@PathParam("id") int id) {
        boolean success = bookService.deleteBook(id);
        if (success) {
            return Response.ok("{\"message\":\"Book deleted successfully\"}").build();
        } else {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"message\":\"Failed to delete book\"}")
                    .build();
        }
    }
}
