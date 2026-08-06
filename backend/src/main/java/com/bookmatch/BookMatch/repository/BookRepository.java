package com.bookmatch.BookMatch.repository;

import com.bookmatch.BookMatch.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookRepository extends JpaRepository<Book, Long> {

    List<Book> findByTagsContainingIgnoreCase(String keyword);
}