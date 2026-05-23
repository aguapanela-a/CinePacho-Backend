package CinePacho.demo.movie.repository;

import CinePacho.demo.movie.entities.MovieScreening;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface MovieScreeningRepository extends JpaRepository<MovieScreening, UUID> { }
