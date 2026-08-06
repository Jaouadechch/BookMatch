package com.bookmatch.BookMatch.service;

import com.bookmatch.BookMatch.entity.Book;
import com.bookmatch.BookMatch.exception.BookNotFoundException;
import com.bookmatch.BookMatch.exception.InvalidRatingException;
import com.bookmatch.BookMatch.repository.BookRepository;
import org.springframework.stereotype.Service;

@Service
public class BookService {

    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public Book rateBook(Long bookId, double rating) {

        if (rating < 1 || rating > 5) {
            throw new InvalidRatingException();
        }

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new BookNotFoundException(bookId));

        book.setRating(rating);

        return bookRepository.save(book);
    }
}