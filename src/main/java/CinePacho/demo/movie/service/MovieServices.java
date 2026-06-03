package CinePacho.demo.movie.service;

import CinePacho.demo.exception.CinePachoException;
import CinePacho.demo.movie.dto.request.GenreDto;
import CinePacho.demo.movie.dto.request.TmdbMovieDTO;
import CinePacho.demo.movie.dto.response.*;
import CinePacho.demo.movie.entities.MovieEntity;
import CinePacho.demo.movie.entities.MovieScreening;
import CinePacho.demo.movie.repository.MovieRepository;
import CinePacho.demo.movie.repository.MovieScreeningRepository;
import CinePacho.demo.shared.auxiliaryClass.RoomManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class MovieServices {
    private final RoomManager roomManager;
    private final MovieScreeningRepository movieScreeningRepository;
    private final MovieRepository movieRepository;
    private final WebClient webClient;

    @Value("${tmdb.access.token}")
    private String accessToken;

    private static final int LIMIT = 4;

    public MovieServices(RoomManager roomManager, MovieScreeningRepository movieScreeningRepository, MovieRepository movieRepository, WebClient webClient) {
        this.roomManager = roomManager;
        this.movieScreeningRepository = movieScreeningRepository;
        this.movieRepository = movieRepository;
        this.webClient = webClient;
    }

    private List<MovieScreening> movieScreeningsByMultiplexId(UUID multiplexId) {
        List<UUID> roomIdsByMultiplex = roomManager.getRoomIdsByMultiplexId(multiplexId);
        if (roomIdsByMultiplex.isEmpty()) {
            return List.of();
        }
        return movieScreeningRepository.findDistinctByRoom_IdInOrderByDateTimeAsc(roomIdsByMultiplex);
    }

    @Transactional(readOnly = true)
    public List<MovieSelectorDTO> getMovieSelectorsByMultiplex(UUID multiplexId) {
        Map<Long, List<MovieScreening>> screeningsByMovie = movieScreeningsByMultiplexId(multiplexId)
                .stream()
                .collect(Collectors.groupingBy(movieScreening -> movieScreening.getMovie().getId()));

        return screeningsByMovie.values()
                .stream()
                .map(this::toMovieSelectorDTO)
                .sorted(Comparator.comparing(
                        movieSelectorDTO -> movieSelectorDTO.movieInfo().originalTitle(),
                        Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)
                ))
                .toList();
    }


    @Transactional(readOnly = true)
    public String getMovieTrailer(Long movieId) {

        TrailerResponseDTO movieKey = webClient.get()
                .uri("/movie/" + movieId + "/videos?language=es-MX")
                .header("Authorization", "Bearer " + accessToken)
                .header("accept", "application/json")
                .retrieve()
                .bodyToMono(TrailerResponseDTO.class)
                .block();

        if (movieKey == null || movieKey.results() == null || movieKey.results().isEmpty()) {
            return "No hay trailer disponible para esta película";
        }

        return movieKey.results().stream()
                .filter(r -> "Trailer".equals(r.type()) && "MX".equals(r.iso_3166_1()))
                .map(Results::key)
                .findFirst()
                .orElse("No hay trailer disponible para esta película");
    }

    // --- NUEVO: MÉTODO PARA OBTENER CRÉDITOS DE TMDB ---
    private Map<String, String> getMovieCredits(Long movieId) {
        try {
            Map response = webClient.get()
                    .uri("/movie/" + movieId + "/credits?language=es-MX")
                    .header("Authorization", "Bearer " + accessToken)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response == null) return Map.of("director", "No disponible", "cast", "No disponible");

            List<Map<String, Object>> crew = (List<Map<String, Object>>) response.get("crew");
            String director = crew != null ? crew.stream()
                    .filter(c -> "Director".equals(c.get("job")))
                    .map(c -> (String) c.get("name"))
                    .findFirst().orElse("No disponible") : "No disponible";

            List<Map<String, Object>> cast = (List<Map<String, Object>>) response.get("cast");
            String castList = cast != null ? cast.stream()
                    .limit(4)
                    .map(c -> (String) c.get("name"))
                    .collect(Collectors.joining(", ")) : "No disponible";

            return Map.of("director", director, "cast", castList);
        } catch (Exception e) {
            return Map.of("director", "No disponible", "cast", "No disponible");
        }
    }

    @Transactional(readOnly = true)
    public List<MovieSelectorDTO> searchMovieSelectorsByMultiplex(UUID multiplexId, String query) {
        if (query == null || query.isBlank()) {
            return getMovieSelectorsByMultiplex(multiplexId).stream().limit(LIMIT).toList();
        }
        String normalizedQuery = query.trim().toLowerCase(Locale.ROOT);
        return getMovieSelectorsByMultiplex(multiplexId)
                .stream()
                .filter(movieSelectorDTO -> titleContains(movieSelectorDTO, normalizedQuery))
                .limit(LIMIT)
                .toList();
    }

    @Transactional(readOnly = true)
    public MovieSelectorDTO getMovieSelectorByMultiplexAndMovie(UUID multiplexId, Long movieId) {
        MovieEntity movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new CinePachoException("La pelicula no existe en la base de datos"));

        List<MovieScreening> movieScreeningsByMovie = movieScreeningsByMultiplexId(multiplexId)
                .stream()
                .filter(movieScreening -> movieScreening.getMovie().getId().equals(movieId))
                .toList();

        if (movieScreeningsByMovie.isEmpty()) {
            throw new CinePachoException("No tenemos funciones disponibles en este multiplex para la película seleccionada");
        }
        return toMovieSelectorDTO(movie, movieScreeningsByMovie);
    }

    private MovieSelectorDTO toMovieSelectorDTO(List<MovieScreening> movieScreenings) {
        MovieEntity movie = movieScreenings.getFirst().getMovie();
        return toMovieSelectorDTO(movie, movieScreenings);
    }

    private MovieSelectorDTO toMovieSelectorDTO(MovieEntity movie, List<MovieScreening> movieScreenings) {
        List<ScreeningInfoDTO> screeningInfoDTOList = movieScreenings.stream()
                .map(this::toScreeningInfoDTO)
                .toList();

        return MovieSelectorDTO.builder()
                .movieInfo(toTmdbMovieDTO(movie))
                .rating(movie.getRating())
                .screenings(screeningInfoDTOList)
                .key(getMovieTrailer(movie.getId()))
                .build();
    }

    private ScreeningInfoDTO toScreeningInfoDTO(MovieScreening screeningMovie) {
        return ScreeningInfoDTO.builder()
                .screeningId(screeningMovie.getId())
                .roomId(screeningMovie.getRoom().getId())
                .roomNumber(screeningMovie.getRoom().getRoomNumber())
                .screeningDate(screeningMovie.getDateTime())
                .status(screeningMovie.getStatus())
                .format(screeningMovie.getFormat() != null ? screeningMovie.getFormat().getDisplayName() : "2D")
                .build();
    }

    private TmdbMovieDTO toTmdbMovieDTO(MovieEntity movie) {
        // Obtenemos los créditos llamando al nuevo método
        Map<String, String> credits = getMovieCredits(movie.getId());

        return TmdbMovieDTO.builder()
                .id(movie.getId())
                .backdropPath(movie.getBackdropPath())
                .genreIds(toGenreDtoList(movie))
                .originalLanguage(movie.getOriginalLanguage())
                .originalTitle(movie.getOriginalTitle())
                .overview(movie.getOverview())
                .posterPath(movie.getPosterPath())
                .releaseDate(movie.getReleaseDate())
                .director(credits.get("director"))
                .cast(credits.get("cast"))
                .build();
    }

    private List<GenreDto> toGenreDtoList(MovieEntity movie) {
        return movie.getGenres().stream()
                .map(genreEmbeddable -> new GenreDto(genreEmbeddable.getId(), genreEmbeddable.getName()))
                .toList();
    }

    private boolean titleContains(MovieSelectorDTO movieSelectorDTO, String normalizedQuery) {
        String title = movieSelectorDTO.movieInfo().originalTitle();
        return title != null && title.toLowerCase(Locale.ROOT).contains(normalizedQuery);
    }
}