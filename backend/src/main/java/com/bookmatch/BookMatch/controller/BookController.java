package com.bookmatch.BookMatch.controller;

import com.bookmatch.BookMatch.entity.Book;
import com.bookmatch.BookMatch.service.BookService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/books")
@CrossOrigin(origins = "*")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @PutMapping("/{id}/rating")
    public Book rateBook(
            @PathVariable Long id,
            @RequestParam double rating
    ) {
        return bookService.rateBook(id, rating);
    }
}