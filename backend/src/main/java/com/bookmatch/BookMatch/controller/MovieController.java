package com.bookmatch.BookMatch.controller;

import com.bookmatch.BookMatch.dto.MovieDto;
import com.bookmatch.BookMatch.service.MovieService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/movies")
@CrossOrigin(origins = "http://localhost:5173")
public class MovieController {

    private final MovieService movieService;

    public MovieController(MovieService movieService) {
        this.movieService = movieService;
    }

    @GetMapping("/search")
    public MovieDto searchMovie(
            @RequestParam("query") String query
    ) {
        return movieService.searchMovie(query);
    }
}