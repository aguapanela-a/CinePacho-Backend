package CinePacho.demo.movie.service;

import CinePacho.demo.exception.CinePachoException;
import CinePacho.demo.movie.entities.MovieEntity;
import CinePacho.demo.movie.entities.MovieScreening;
import CinePacho.demo.movie.repository.MovieRepository;
import CinePacho.demo.movie.repository.MovieScreeningRepository;
import CinePacho.demo.shared.auxiliaryClass.MovieManager;
import org.springframework.stereotype.Component;

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
}
