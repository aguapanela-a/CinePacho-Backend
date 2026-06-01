package CinePacho.demo.movie.service;

import CinePacho.demo.exception.CinePachoException;
import CinePacho.demo.movie.entities.MovieEntity;
import CinePacho.demo.movie.entities.MovieScreening;
import CinePacho.demo.movie.repository.MovieRepository;
import CinePacho.demo.movie.repository.MovieScreeningRepository;
import CinePacho.demo.shared.auxiliaryClass.MovieManager;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Component
public class MovieManagerImpl implements MovieManager {

    private final MovieRepository movieRepository;
    private final MovieScreeningRepository movieScreeningRepository;

    public MovieManagerImpl(MovieRepository movieRepository, MovieScreeningRepository movieScreeningRepository) {
        this.movieRepository = movieRepository;
        this.movieScreeningRepository = movieScreeningRepository;
    }

    @Override
    public boolean existsById(Long id) {
        return id != null && movieRepository.existsById(id);
    }

    @Override
    public Double getRating(Long id) {
        MovieEntity movie = movieRepository.findById(id).orElseThrow(()-> new CinePachoException("Película no encontrada"));
        return movie.getRating();
    }

    @Override
    public void updateRating(Long id, Double rating) {
        MovieEntity movie = movieRepository.getReferenceById(id);
        movie.setRating(rating);
        movieRepository.save(movie);
    }

    @Override
    public String getMovieTitle(Long id) {
        return movieRepository.getReferenceById(id).getOriginalTitle();
    }

    @Override
    public Long getMovieIdByScreeningId(java.util.UUID screeningId) {
        MovieScreening screening = movieScreeningRepository.findById(screeningId)
                .orElseThrow(() -> new CinePachoException("Función no encontrada"));
        return screening.getMovie().getId();
    }

    @Override
    public MovieScreening getMovieScreeningById(UUID id) {
        return movieScreeningRepository.findById(id).orElseThrow(()-> new CinePachoException("Función no encontrada"));
    }

    @Override
    public List<MovieScreening> findByDateTimeAfter(LocalDateTime dateTime) {
        return movieScreeningRepository.findAllByDateTimeAfter(dateTime);
    }

    @Override
    public List<MovieScreening> findByDateBefore(LocalDateTime dateTime) {
        return movieScreeningRepository.findAllByDateTimeBefore(dateTime);
    }

    @Override
    public void save(MovieScreening movieScreening) {
        movieScreeningRepository.save(movieScreening);
    }
}
