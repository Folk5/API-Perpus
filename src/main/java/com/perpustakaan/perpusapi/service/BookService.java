package com.perpustakaan.perpusapi.service;

import com.perpustakaan.perpusapi.model.Book;
import com.perpustakaan.perpusapi.repo.BookRepo;

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
        try {
            bookRepository.insert(book);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateBook(Book book) {
        try {
            // Cek dulu apakah buku dengan ID itu ada
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
