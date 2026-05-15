package CinePacho.demo.movie.service;

import CinePacho.demo.movie.dto.MovieResponseDTO;
import CinePacho.demo.movie.dto.TmdbMovieDTO;
import CinePacho.demo.movie.dto.TmdbResponseDTO;
import CinePacho.demo.movie.entities.MovieEntity;
import CinePacho.demo.movie.repository.MovieRepository;
import CinePacho.demo.movie.repository.MovieScreeningRepository;
import org.jspecify.annotations.NonNull;
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
    // el front cunado selecciona una, tendrá un json con la estructura de TmdbMovieDTO
    // pero envia solo id para evitar errores de escritura

    public MovieResponseDTO addMovie(Long id) {

        TmdbMovieDTO movieDTO = webClient.get()
                .uri("/movie/"+id+"?language=es")
                .header("accept", "application/json")
                .header("Authorization","Bearer "+accessToken)
                .retrieve()
                .bodyToMono(TmdbMovieDTO.class)
                .block();


        MovieEntity movieEntity = getMovieEntity(movieDTO);


        movieRepository.save(movieEntity);

        return new MovieResponseDTO(movieEntity.getOriginalTitle(), movieEntity.getDirector(), "Película añadida con éxito");
    }

    private static @NonNull MovieEntity getMovieEntity(TmdbMovieDTO movieDTO) {
        MovieEntity  movieEntity = new MovieEntity();

        movieEntity.setId(movieDTO.id());
        movieEntity.setBackdropPath(movieDTO.backdropPath());
        movieEntity.setGenres(movieDTO.genreIds());
        movieEntity.setOriginalLanguage(movieDTO.originalLanguage());
        movieEntity.setOriginalTitle(movieDTO.originalTitle());
        movieEntity.setOverview(movieDTO.overview());
        movieEntity.setPosterPath(movieDTO.posterPath());
        movieEntity.setReleaseDate(movieDTO.releaseDate());

        movieEntity.setRating(null);
        return movieEntity;
    }

}
