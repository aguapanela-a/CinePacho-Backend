package CinePacho.demo.movie.service;

import CinePacho.demo.movie.dto.TmdbMovieDTO;
import CinePacho.demo.movie.dto.TmdbResponseDTO;
import CinePacho.demo.movie.repository.MovieRepository;
import CinePacho.demo.movie.repository.MovieScreeningRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class MovieService {

    private final MovieRepository movieRepository;
    private final MovieScreeningRepository movieScreeningRepository;
    private final WebClient webClient;

    @Value("${tmdb.access.token}")
    private String accessToken;

    @Autowired
    public MovieService(MovieRepository movieRepository, MovieScreeningRepository movieScreeningRepository, WebClient webClient) {
        this.movieRepository = movieRepository;
        this.movieScreeningRepository = movieScreeningRepository;
        this.webClient = webClient;
    }

    public List<TmdbMovieDTO> searchMovie(String title, int page) {
        List<TmdbMovieDTO> tmdbMovieDTOList;

        tmdbMovieDTOList = Objects.requireNonNull(webClient.get()
                        .uri("/search/movie?query=" + title + "&language=es&page=" + page)
                        .header("accept", "application/json")
                        .header("Authorization", "Bearer " + accessToken)
                        .retrieve()
                        .bodyToMono(TmdbResponseDTO.class)
                        .block())
                .results();

        return tmdbMovieDTOList;
    }

    //Cuando da click a una peli de la lista de arriba, se ejecutará este metodo
//    public void addMovie(Long id) {
//
//    }
}
