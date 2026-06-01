package CinePacho.demo.movie.controller;

import CinePacho.demo.movie.dto.response.MovieListingResponseDTO;
import CinePacho.demo.movie.dto.request.CreateScreeningDTO;
import CinePacho.demo.movie.dto.response.MovieResponseDTO;
import CinePacho.demo.movie.dto.response.MovieSearchResponseDTO;
import CinePacho.demo.movie.dto.response.MovieSelectorDTO;
import CinePacho.demo.movie.dto.response.ScreeningResponseDTO;
import CinePacho.demo.movie.enumeration.ScreeningStatus;
import CinePacho.demo.movie.service.MovieScreeningService;
import CinePacho.demo.movie.service.MovieAdminService;
import CinePacho.demo.movie.service.MovieServices;
import CinePacho.demo.shared.auxiliaryClass.DTOResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class MovieController {

    private final MovieServices movieServices;
    private final MovieScreeningService movieScreeningService;
    private final MovieAdminService movieAdminService;

    @Autowired
    public MovieController(MovieServices movieServices, MovieScreeningService movieScreeningService, MovieAdminService movieAdminService) {
        this.movieServices = movieServices;
        this.movieScreeningService = movieScreeningService;
        this.movieAdminService = movieAdminService;
    }

    //Endpoint de búsqueda de movies por multiplex (cartelera de un multiplex)
    @GetMapping("/movie/multiplex/{multiplexId}/selectors")
    public ResponseEntity<List<MovieSelectorDTO>> getMovieSelectorsByMultiplex(
            @PathVariable UUID multiplexId,
            @RequestParam(required = false) String query
    ) {
        return ResponseEntity.ok(movieServices.searchMovieSelectorsByMultiplex(multiplexId, query));
    }

    //Endpoint para obtener una unica "info" de peli en la cartelera de un multiplex
    @GetMapping("/movie/multiplex/{multiplexId}/selectors/{movieId}")
    public ResponseEntity<MovieSelectorDTO> getMovieSelectorByMultiplexAndMovie(
            @PathVariable UUID multiplexId,
            @PathVariable Long movieId
    ) {
        return ResponseEntity.ok(movieServices.getMovieSelectorByMultiplexAndMovie(multiplexId, movieId));
    }

    //solo puede acceder buyer
    //endpoitn paa obtener las 8 peliculas en cartelera de un multiplex
    @GetMapping("/movie/multiplex/{multiplexId}")
    public ResponseEntity<List<MovieListingResponseDTO>> getMovieListingByMultiplex(
            @PathVariable UUID multiplexId
    ) {
        return ResponseEntity.ok(movieScreeningService.getTop8ByMultiplexId(multiplexId));
    }

    //Metodo para obtener el string de la key
    //Modo de uso en front: https://www.youtube.com/watch?v={key}
    @GetMapping("/movie/trailer/{movieId}")
    public ResponseEntity<String> getMovieTrailer(@PathVariable Long movieId) {
        return ResponseEntity.ok(movieServices.getMovieTrailer(movieId));
    }

    //Top 10 peliculas con más rating global
    @GetMapping("/topRatedMovies")
    public ResponseEntity<List<MovieListingResponseDTO>> getTopRatedMovies() {
        return ResponseEntity.ok(movieScreeningService.getTop10Movies());
    }

    //Buscar la película en la API externa
    @GetMapping("/admin/movie/search")
    public ResponseEntity<List<MovieSearchResponseDTO>> searchMovie(
            @Valid
            @RequestParam String query,
            @RequestParam(defaultValue = "1") int page
    ) {
        return ResponseEntity.ok(movieAdminService.searchMovie(query, page));
    }


    @PostMapping("/admin/movie/select/{movieId}")
    public ResponseEntity<MovieResponseDTO> selectMovie(
            @Valid
            @PathVariable Long movieId
    ) {
        return ResponseEntity.ok(movieAdminService.selectMovie(movieId));
    }


    @PostMapping("/admin/movie/createScreening")
    public ResponseEntity<ScreeningResponseDTO> createScreening(
            @Valid
            @RequestBody CreateScreeningDTO dto
    ) {
        return ResponseEntity.ok(movieAdminService.createScreening(dto));
    }


    @PutMapping("/admin/movie/changeStatus/{idScreening}")
    public ResponseEntity<DTOResponse> deleteMovie(
            @Valid
            @PathVariable UUID idScreening,
            @RequestParam ScreeningStatus status
    ){
        movieAdminService.changeScreeningStatus(idScreening, status);

        // Respuesta estándar para actualización de estado
        DTOResponse response = DTOResponse.withStatus(
                "Estado de la función actualizado correctamente",
                HttpStatus.OK.value()
        );
        return ResponseEntity.ok(response);
    }

}
