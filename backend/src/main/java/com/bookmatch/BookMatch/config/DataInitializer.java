package com.bookmatch.BookMatch.config;

import com.bookmatch.BookMatch.entity.Book;
import com.bookmatch.BookMatch.repository.BookRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initBooks(BookRepository bookRepository) {

        return args -> {

            if (bookRepository.count() == 0) {

                List<Book> books = List.of(

                        new Book(
                                "Dune",
                                "Frank Herbert",
                                "Epic science fiction.",
                                "Sci-Fi",
                                "space,future,desert,politics",
                                4.8
                        ),

                        new Book(
                                "Project Hail Mary",
                                "Andy Weir",
                                "Astronaut saves humanity.",
                                "Sci-Fi",
                                "space,science,survival",
                                4.9
                        ),

                        new Book(
                                "Harry Potter and the Philosopher's Stone",
                                "J.K. Rowling",
                                "Wizard school.",
                                "Fantasy",
                                "magic,wizard,school,fantasy",
                                4.9
                        ),

                        new Book(
                                "The Hobbit",
                                "J.R.R. Tolkien",
                                "Adventure in Middle-earth.",
                                "Fantasy",
                                "fantasy,dragon,adventure",
                                4.8
                        ),

                        new Book(
                                "Neuromancer",
                                "William Gibson",
                                "Cyberpunk classic.",
                                "Sci-Fi",
                                "mind,technology,hacker,future",
                                4.5
                        )

                );

                bookRepository.saveAll(books);

                System.out.println("Sample books inserted successfully.");
            }

        };
    }
}