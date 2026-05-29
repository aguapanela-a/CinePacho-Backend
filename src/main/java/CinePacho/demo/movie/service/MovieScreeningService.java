package CinePacho.demo.movie.service;

import java.util.List;

import org.springframework.stereotype.Service;

import CinePacho.demo.movie.dto.MovieListingResponseDTO;
import CinePacho.demo.movie.entities.GenreEmbeddable;
import CinePacho.demo.movie.repository.MovieScreeningRepository;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@Service
public class MovieScreeningService {
    
    private final MovieScreeningRepository movieScreeningRepository;


    // public

    //Top 10 movies global (BD central)
    public List<MovieListingResponseDTO> getTop10Movies() {
        return movieScreeningRepository.findAll().stream().sorted(
            (movieScreening1, movieScreening2) -> Double.compare(movieScreening2.getMovie().getRating(), movieScreening1.getMovie().getRating())
        ).limit(10).map(
            movieScreeningEntity -> new MovieListingResponseDTO(
                    movieScreeningEntity.getMovie().getId(),
                    movieScreeningEntity.getMovie().getOriginalTitle(),
                    movieScreeningEntity.getMovie().getGenres().stream()
                        .map(GenreEmbeddable::getName)
                            .toList(),
                        Integer.parseInt(movieScreeningEntity.getMovie().getReleaseDate().substring(0, 4)),
                        movieScreeningEntity.getMovie().getPosterPath(),
                        movieScreeningEntity.getMovie().getBackdropPath()
                    ))
                    .toList();
    }

}
