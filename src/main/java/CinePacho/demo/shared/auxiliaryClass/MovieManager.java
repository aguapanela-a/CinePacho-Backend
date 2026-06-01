package CinePacho.demo.shared.auxiliaryClass;

import CinePacho.demo.movie.entities.MovieScreening;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Component
public interface MovieManager {
    boolean existsById(Long id);
    Double getRating(Long id);
    void updateRating(Long id, Double rating);
    String getMovieTitle(Long id);
    Long getMovieIdByScreeningId(UUID screeningId);
    MovieScreening getMovieScreeningById(UUID id);
    List<MovieScreening> findByDateTimeAfter(LocalDateTime dateTime);
    List<MovieScreening> findByDateBefore(LocalDateTime dateTime);
    void save(MovieScreening movieScreening);
}
