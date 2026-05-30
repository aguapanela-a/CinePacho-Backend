package CinePacho.demo.movie.service;

import CinePacho.demo.exception.CinePachoException;
import CinePacho.demo.movie.dto.request.GenreDto;
import CinePacho.demo.movie.dto.request.TmdbMovieDTO;
import CinePacho.demo.movie.dto.response.MovieSelectorDTO;
import CinePacho.demo.movie.dto.response.ScreeningInfoDTO;
import CinePacho.demo.movie.dto.response.TrailerResponseDTO;
import CinePacho.demo.movie.dto.response.MovieListingResponseDTO;
import CinePacho.demo.movie.entities.MovieEntity;
import CinePacho.demo.movie.entities.MovieScreening;
import CinePacho.demo.movie.repository.MovieRepository;
import CinePacho.demo.movie.repository.MovieScreeningRepository;
import CinePacho.demo.shared.auxiliaryClass.RoomManager;
import lombok.AllArgsConstructor;
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

@AllArgsConstructor
@Service
public class MovieServices {
    private final RoomManager roomManager;
    private final MovieScreeningRepository movieScreeningRepository;
    private final MovieRepository movieRepository;
    private final WebClient webClient;

    @Value("${tmdb.access.token}")
    private String accessToken;

    private static final int LIMIT = 4;

    // Provee la lista de movieScreenings por multiplexId sin acoplar movie al repositorio de rooms.
    private List<MovieScreening> movieScreeningsByMultiplexId(UUID multiplexId) {
        List<UUID> roomIdsByMultiplex = roomManager.getRoomIdsByMultiplexId(multiplexId);

        if (roomIdsByMultiplex.isEmpty()) {
            return List.of();
        }

        return movieScreeningRepository.findDistinctByRoom_IdInOrderByDateTimeAsc(roomIdsByMultiplex);
    }

    //metodo para obtener todas las MovieSelectorDTO en la cartelera de un multiplex
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

    //metodo para buscar la key del trailer de una pelicula
    @Transactional(readOnly = true)
    public String getMovieTrailer(Long movieId) {
        TrailerResponseDTO movieKey = webClient.get()
                .uri("/movie/"+movieId+"/videos?language=es-mx")
                .header("Authorization: Bearer "+accessToken)
                .header("accept: application/json")
                .retrieve()
                .bodyToMono(TrailerResponseDTO.class)
                .block();

        assert movieKey != null;
        if (movieKey.results().isEmpty()) {
            throw new CinePachoException("Esta película no tiene trailer :(");
        }

        return movieKey.results().getFirst().key();
    }

    //metodo para buscar MovieSelectorDTO por multiplex y por titulo
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

    //metodo para obtener una unica "MovieSelectorDTO" de peli en la cartelera
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

    // metodos auxiliares aplicando separacion de responsabilidades
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
                .build();
    }

    private TmdbMovieDTO toTmdbMovieDTO(MovieEntity movie) {
        return TmdbMovieDTO.builder()
                .id(movie.getId())
                .backdropPath(movie.getBackdropPath())
                .genreIds(toGenreDtoList(movie))
                .originalLanguage(movie.getOriginalLanguage())
                .originalTitle(movie.getOriginalTitle())
                .overview(movie.getOverview())
                .posterPath(movie.getPosterPath())
                .releaseDate(movie.getReleaseDate())
                .director(movie.getDirector())
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
