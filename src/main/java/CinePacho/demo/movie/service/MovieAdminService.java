package CinePacho.demo.movie.service;

import CinePacho.demo.exception.CinePachoException;
import CinePacho.demo.movie.dto.request.CreateScreeningDTO;
import CinePacho.demo.movie.dto.request.TmdbMovieDTO;
import CinePacho.demo.movie.dto.response.MovieResponseDTO;
import CinePacho.demo.movie.dto.response.MovieSearchResponseDTO;
import CinePacho.demo.movie.dto.response.ScreeningResponseDTO;
import CinePacho.demo.movie.dto.response.TmdbResponseDTO;
import CinePacho.demo.movie.entities.GenreEmbeddable;
import CinePacho.demo.movie.entities.MovieEntity;
import CinePacho.demo.movie.entities.MovieScreening;
import CinePacho.demo.movie.enumeration.ScreeningStatus;
import CinePacho.demo.movie.repository.MovieRepository;
import CinePacho.demo.movie.repository.MovieScreeningRepository;
import CinePacho.demo.rooms.entities.RoomEntity;
import CinePacho.demo.shared.auxiliaryClass.RoomManager;
import CinePacho.demo.shared.serviceSecurity.AccessValidator;
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
public class MovieAdminService {

    private final MovieRepository movieRepository;
    private final MovieScreeningRepository movieScreeningRepository;
    private final WebClient webClient;
    private final RoomManager roomManager;
    private final AccessValidator accessValidator;

    @Value("${tmdb.access.token}")
    private String accessToken;

    @Autowired
    public MovieAdminService(MovieRepository movieRepository, MovieScreeningRepository movieScreeningRepository, WebClient webClient, RoomManager roomManager, AccessValidator accessValidator) {
        this.movieRepository = movieRepository;
        this.movieScreeningRepository = movieScreeningRepository;
        this.webClient = webClient;

        this.roomManager = roomManager;
        this.accessValidator = accessValidator;
    }


    // el front debe desplegar una lista dinámica de películas según el character ingresado
    public List<MovieSearchResponseDTO> searchMovie(String title, int page) {
        List<MovieSearchResponseDTO> movieSearchResponseDTO;

        movieSearchResponseDTO = Objects.requireNonNull(webClient.get()
                        .uri("/search/movie?query=" + title + "&language=es&page=" + page)
                        .header("accept", "application/json")
                        .header("Authorization", "Bearer " + accessToken)
                        .retrieve()
                        .bodyToMono(TmdbResponseDTO.class)
                        .block())
                .results();

        return movieSearchResponseDTO;
    }

    //Cuando da clic a una peli de la lista de arriba, se ejecutará este siguiente metodo,
    // el front cunado selecciona una, tendrá un json con la estructura de TmdbMovieDTO
    // pero envía solo id para evitar errores de escritura

    public MovieResponseDTO selectMovie(Long id) {

        //Busca primero en BD
        if (movieRepository.existsById(id)) {
            MovieEntity movieEntity = movieRepository.getReferenceById(id);
            return new MovieResponseDTO(movieEntity.getOriginalTitle(), "Película seleccionada con éxito");
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

        System.out.println("Genres:");
        // esta lista está vacía porque noe s uan lista de enteros sino que es de "objetos"
        movieDTO.genreIds().stream().forEach(System.out::println);

        System.out.printf("___________-------________");

        MovieEntity movieEntity = getMovieEntity(movieDTO);


        movieRepository.save(movieEntity);

        return new MovieResponseDTO(
            movieEntity.getOriginalTitle(), 
            "Película añadida con éxito"
        );
    }


    public ScreeningResponseDTO createScreening(CreateScreeningDTO createScreeningDTO) {

        //verifica que traiga una película seleccionada
        MovieEntity movie = movieRepository.findById(createScreeningDTO.movieId()) //buca en BD por id
                .orElseThrow(() -> new CinePachoException("Debes seleccionar una película primero")); // si no encuentra nada es porque no ha seleccionado

        // traer sala para asociar a la función
        RoomEntity room = roomManager.getRoom(createScreeningDTO.roomId());
        // Valida que el gerente sólo cree funciones en su multiplex
        accessValidator.validateMultiplexAccess(room.getMultiplex().getId());

        MovieScreening movieScreening = new MovieScreening();
        movieScreening.setMovie(movie);
        movieScreening.setRoom(room);
        movieScreening.setDateTime(createScreeningDTO.dateTime());
        movieScreening.setStatus(ScreeningStatus.ACTIVE);

        MovieScreening screening = movieScreeningRepository.save(movieScreening);

        return new ScreeningResponseDTO(
                screening.getId(),
                movieScreening.getDateTime(),
                movie.getOriginalLanguage(),
                movie.getOriginalTitle(),
                movie.getOverview(),
                movie.getRating(),
                screening.getStatus(),
                getGenreList(movie)
        );
    }


    public void changeScreeningStatus(UUID id, ScreeningStatus screeningStatus) {

        MovieScreening movieScreening = movieScreeningRepository.findById(id)
                .orElseThrow(()-> new CinePachoException("Película no encontrada, por favor asegúrese de escribir bien el id"));

        // Valida que el gerente sólo cambie funciones de su multiplex
        accessValidator.validateMultiplexAccess(movieScreening.getRoom().getMultiplex().getId());

        movieScreening.setStatus(screeningStatus);
        movieScreeningRepository.save(movieScreening);
    }

    private List<String> getGenreList(MovieEntity movieEntity) {
        List<String> genreList = new ArrayList<>();

        // Extraer directamente los nombres de los géneros almacenados en la película
        movieEntity.getGenres().forEach(genre -> genreList.add(genre.getName()));

        return genreList;
    }


    private static @NonNull MovieEntity getMovieEntity(TmdbMovieDTO movieDTO) {
        MovieEntity movieEntity = new MovieEntity();

        movieEntity.setId(movieDTO.id());
        movieEntity.setBackdropPath(movieDTO.backdropPath());
        // Mapear GenreDto -> GenreEmbeddable (id + name)
        List<GenreEmbeddable> genreEmbeddables = new ArrayList<>();
        if (movieDTO.genreIds() != null) {
            movieDTO.genreIds().forEach(gdto -> genreEmbeddables.add(new GenreEmbeddable(gdto.id(), gdto.name())));
        }


        movieEntity.setGenres(genreEmbeddables);
        movieEntity.setOriginalLanguage(movieDTO.originalLanguage());
        movieEntity.setOriginalTitle(movieDTO.originalTitle());
        movieEntity.setOverview(movieDTO.overview());
        movieEntity.setPosterPath(movieDTO.posterPath());
        movieEntity.setReleaseDate(movieDTO.releaseDate());

        movieEntity.setRating(2.5); // Inicializa el rating en 2.5, se actualizará cuando los usuarios califiquen la película
        return movieEntity;
    }

}
