package CinePacho.demo.movie.service;

import CinePacho.demo.exception.CinePachoException;
import CinePacho.demo.movie.dto.*;
import CinePacho.demo.movie.entities.MovieEntity;
import CinePacho.demo.movie.entities.MovieScreening;
import CinePacho.demo.movie.enumeration.ScreeningStatus;
import CinePacho.demo.movie.repository.MovieRepository;
import CinePacho.demo.movie.repository.MovieScreeningRepository;
import CinePacho.demo.rooms.entities.RoomEntity;
import CinePacho.demo.shared.auxiliaryClass.MultiplexProvider;
import CinePacho.demo.shared.auxiliaryClass.RoomProvider;
import CinePacho.demo.shared.tmdbGenre.TmdbGenreMapper;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class MovieService {

    private final MovieRepository movieRepository;
    private final MovieScreeningRepository movieScreeningRepository;
    private final WebClient webClient;
    private final RoomProvider  roomProvider;

    @Value("${tmdb.access.token}")
    private String accessToken;

    @Autowired
    public MovieService(MovieRepository movieRepository, MovieScreeningRepository movieScreeningRepository, WebClient webClient, MultiplexProvider multiplexProvider, RoomProvider roomProvider) {
        this.movieRepository = movieRepository;
        this.movieScreeningRepository = movieScreeningRepository;
        this.webClient = webClient;

        this.roomProvider = roomProvider;
    }


    // el front debe desplegar una lista dinámica de películas según el character ingresado
    public List<TmdbMovieDTO> searchMovie(String title, int page) {
        List<TmdbMovieDTO> tmdbMovieDTOList;

        tmdbMovieDTOList = Objects.requireNonNull(webClient.get()
                        .uri("/search/movie?query=" + title + "&language=es&page=" + page)
                        .header("accept", "application/json")
                        .header("Authorization", "Bearer " + accessToken)
                        .retrieve()
                        .bodyToMono(TmdbResponseDTO.class)
                        .block())
                .results();

        return tmdbMovieDTOList;
    }

    //Cuando da clic a una peli de la lista de arriba, se ejecutará este siguiente metodo,
    // el front cunado selecciona una, tendrá un json con la estructura de TmdbMovieDTO
    // pero envía solo id para evitar errores de escritura

    public MovieResponseDTO selectMovie(Long id) {

        //Busca primero en BD
        if (movieRepository.existsById(id)) {
            MovieEntity movieEntity = movieRepository.getReferenceById(id);
            return new MovieResponseDTO(movieEntity.getOriginalTitle(), movieEntity.getDirector(), "Película seleccionada con éxito");
        }

        // si no está en BD la busca en la API
        TmdbMovieDTO movieDTO = webClient.get()
                .uri("/movie/" + id + "?language=es")
                .header("accept", "application/json")
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(TmdbMovieDTO.class)
                .block();

        // si, por alguna razón, se crea un objeto nulo (no existe ese id de peli), lance una excepción
        if (movieDTO == null) {
            throw new CinePachoException("Petición de película inválida, ¡por favor verifique el id ingresado!");
        }

        MovieEntity movieEntity = getMovieEntity(movieDTO);

        movieRepository.save(movieEntity);

        return new MovieResponseDTO(movieEntity.getOriginalTitle(), movieEntity.getDirector(), "Película añadida con éxito");
    }


    public ScreeningResponseDTO createScreening(CreateScreeningDTO createScreeningDTO) {

        //verifica que traiga una película seleccionada
        MovieEntity movie = movieRepository.findById(createScreeningDTO.movieId()) //buca en BD por id
                .orElseThrow(() -> new CinePachoException("Debes seleccionar una película primero")); // si no encuentra nada es porque no ha seleccionado

        // traer sala para asociar a la función
        RoomEntity room = roomProvider.getRoom(createScreeningDTO.roomId());

        MovieScreening movieScreening = new MovieScreening();
        movieScreening.setMovie(movie);
        movieScreening.setRoom(room);
        movieScreening.setDateTime(createScreeningDTO.dateTime());
        movieScreening.setPrice(createScreeningDTO.price());
        movieScreening.setStatus(ScreeningStatus.ACTIVE);

        MovieScreening screening = movieScreeningRepository.save(movieScreening);

        return new ScreeningResponseDTO(
                screening.getId(),
                movieScreening.getDateTime(),
                movieScreening.getPrice(),
                movie.getOriginalLanguage(),
                movie.getOriginalTitle(),
                movie.getOverview(),
                movie.getRating(),
                movie.getDirector(),
                screening.getStatus(),
                getGenreList(movie)
        );
    }


    public void changeScreeningStatus(UUID id, ScreeningStatus screeningStatus) {

        MovieScreening movieScreening = movieScreeningRepository.findById(id)
                .orElseThrow(()-> new CinePachoException("Película no encontrada, por favor asegúrese de escribir bien el id"));

        movieScreening.setStatus(screeningStatus);
        movieScreeningRepository.save(movieScreening);
    }

    private List<String> getGenreList(MovieEntity movieEntity) {
        TmdbGenreMapper genreMapper = new TmdbGenreMapper();

        List<String> genreList = new ArrayList<>();

        movieEntity.getGenres().forEach(genreId -> {
            genreList.add(genreMapper.getGenreName(genreId));
        });

        return genreList;
    }


    private static @NonNull MovieEntity getMovieEntity(TmdbMovieDTO movieDTO) {
        MovieEntity movieEntity = new MovieEntity();

        movieEntity.setId(movieDTO.id());
        movieEntity.setBackdropPath(movieDTO.backdropPath());
        movieEntity.setGenres(movieDTO.genreIds());
        movieEntity.setOriginalLanguage(movieDTO.originalLanguage());
        movieEntity.setOriginalTitle(movieDTO.originalTitle());
        movieEntity.setOverview(movieDTO.overview());
        movieEntity.setPosterPath(movieDTO.posterPath());
        movieEntity.setReleaseDate(movieDTO.releaseDate());

        movieEntity.setRating(null);
        return movieEntity;
    }

}
