package CinePacho.demo.movie.service;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import CinePacho.demo.movie.dto.response.MovieListingResponseDTO;
import CinePacho.demo.movie.entities.GenreEmbeddable;
import CinePacho.demo.movie.entities.MovieEntity;
import CinePacho.demo.movie.entities.MovieScreening;
import CinePacho.demo.movie.enumeration.ScreeningStatus;
import CinePacho.demo.movie.repository.MovieScreeningRepository;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@Service
public class MovieScreeningService {

    private static final int TOP_GLOBAL_MOVIES_LIMIT = 10;
    private static final int TOP_MULTIPLEX_MOVIES_LIMIT = 8;
    
    private final MovieScreeningRepository movieScreeningRepository;


    // public

    //Top 10 movies global (BD central)
    public List<MovieListingResponseDTO> getTop10Movies() {
        return toTopMovieListing(movieScreeningRepository.findAll(), TOP_GLOBAL_MOVIES_LIMIT);
    }

    public List<MovieListingResponseDTO> getTop8ByMultiplexId(UUID multiplexId) {
       return toTopMovieListing(
               movieScreeningRepository.findAllByRoom_Multiplex_Id(multiplexId)
                       .stream()
                       .filter(this::isActiveScreening)
                       .toList(),
               TOP_MULTIPLEX_MOVIES_LIMIT
       );
    }

    private List<MovieListingResponseDTO> toTopMovieListing(List<MovieScreening> movieScreenings, int limit) {
        return movieScreenings.stream()
                .filter(movieScreening -> movieScreening.getMovie() != null)
                .sorted(Comparator.comparingDouble(this::movieRating).reversed())
                .collect(Collectors.toMap(
                        movieScreening -> movieScreening.getMovie().getId(),
                        movieScreening -> movieScreening,
                        (firstScreening, repeatedScreening) -> firstScreening,
                        LinkedHashMap::new
                ))
                .values()
                .stream()
                .limit(limit)
                .map(this::toMovieListingResponseDTO)
                .toList();
    }

    private MovieListingResponseDTO toMovieListingResponseDTO(MovieScreening movieScreening) {
        MovieEntity movie = movieScreening.getMovie();

        return new MovieListingResponseDTO(
                movie.getId(),
                movie.getOriginalTitle(),
                movie.getGenres().stream()
                        .map(GenreEmbeddable::getName)
                        .toList(),
                releaseYear(movie),
                movie.getPosterPath(),
                movie.getBackdropPath()
        );
    }

    private boolean isActiveScreening(MovieScreening movieScreening) {
        return ScreeningStatus.ACTIVE.equals(movieScreening.getStatus());
    }

    private double movieRating(MovieScreening movieScreening) {
        Double rating = movieScreening.getMovie().getRating();
        return rating == null ? 0.0 : rating;
    }

    private Integer releaseYear(MovieEntity movie) {
        String releaseDate = movie.getReleaseDate();
        if (releaseDate == null || releaseDate.length() < 4) {
            return null;
        }

        try {
            return Integer.parseInt(releaseDate.substring(0, 4));
        } catch (NumberFormatException exception) {
            return null;
        }
    }

}
