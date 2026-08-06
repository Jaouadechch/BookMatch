package com.bookmatch.BookMatch.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "books")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String author;

    @Column(length = 2000)
    private String description;

    private String genre;

    private String tags;

    private double rating;

    private String coverUrl;

    public Book(
            String title,
            String author,
            String description,
            String genre,
            String tags,
            double rating
    ) {
        this.title = title;
        this.author = author;
        this.description = description;
        this.genre = genre;
        this.tags = tags;
        this.rating = rating;
    }
}