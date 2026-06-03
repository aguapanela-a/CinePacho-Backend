package CinePacho.demo.movie.repository;

import CinePacho.demo.movie.entities.MovieEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovieRepository extends JpaRepository<MovieEntity, Long> {
    Long getByOriginalTitle(String originalTitle);
}
