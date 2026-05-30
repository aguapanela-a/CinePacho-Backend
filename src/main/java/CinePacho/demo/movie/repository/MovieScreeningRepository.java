package CinePacho.demo.movie.repository;

import CinePacho.demo.movie.entities.MovieScreening;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface MovieScreeningRepository extends JpaRepository<MovieScreening, UUID> {
    List<MovieScreening> findMovieScreeningByRoom_Id(UUID roomId);
    List<MovieScreening> findDistinctByRoom_IdInOrderByDateTimeAsc(Collection<UUID> roomIds);

    List<MovieScreening> findAllByRoom_Multiplex_Id(UUID roomMultiplexId);

    MovieScreening findByDateTimeAfter(LocalDateTime dateTimeAfter);

    List<MovieScreening> findAllByDateTimeAfter(LocalDateTime dateTimeAfter);

    List<MovieScreening> findAllByDateTimeBefore(LocalDateTime dateTimeBefore);
}
