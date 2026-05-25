package CinePacho.demo.shared.auxiliaryClass;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public interface MovieManager {
    boolean existsById(Long id);
    Double getRating(Long id);
    void updateRating(Long id, Double rating);
    String getMovieTitle(Long id);
    Long getMovieIdByScreeningId(UUID screeningId);
}
