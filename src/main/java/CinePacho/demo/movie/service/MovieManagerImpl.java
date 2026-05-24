package CinePacho.demo.movie.service;

import CinePacho.demo.movie.repository.MovieRepository;
import CinePacho.demo.shared.auxiliaryClass.MovieManager;
import org.springframework.stereotype.Component;

@Component
public class MovieManagerImpl implements MovieManager {

    private final MovieRepository movieRepository;

    public MovieManagerImpl(MovieRepository movieRepository) {
        this.movieRepository = movieRepository;
    }

    @Override
    public boolean existsById(Long id) {
        return id != null && movieRepository.existsById(id);
    }
}
