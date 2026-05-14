package CinePacho.demo.movie.repository;

import CinePacho.demo.movie.entities.MovieEntity;
import CinePacho.demo.movie.entities.MovieScreening;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MovieScreeningRepository extends JpaRepository<MovieScreening, Long> {
    List<MovieScreening> findAllByMovie(MovieEntity movie);
}
