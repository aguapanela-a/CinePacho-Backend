package CinePacho.demo.movie.controller;

import CinePacho.demo.movie.dto.request.CreateScreeningDTO;
import CinePacho.demo.movie.dto.response.MovieResponseDTO;
import CinePacho.demo.movie.dto.response.MovieSearchResponseDTO;
import CinePacho.demo.movie.dto.response.MovieSelectorDTO;
import CinePacho.demo.movie.dto.response.ScreeningResponseDTO;
import CinePacho.demo.movie.enumeration.ScreeningStatus;
import CinePacho.demo.movie.service.MovieAdminService;
import CinePacho.demo.movie.service.MovieServices;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class MovieController {
    private final MovieAdminService movieAdminService;
    private final MovieServices movieServices;

    @Autowired
    public MovieController(MovieAdminService movieAdminService, MovieServices movieServices) {
        this.movieAdminService = movieAdminService;
        this.movieServices = movieServices;
    }

    //Endpoint de búsqueda de movies por multiplex (cartelera de un multiplex)
    @GetMapping("/movie/multiplex/{multiplexId}/selectors")
    public ResponseEntity<List<MovieSelectorDTO>> getMovieSelectorsByMultiplex(
            @PathVariable UUID multiplexId,
            @RequestParam(required = false) String query
    ) {
        return ResponseEntity.ok(movieServices.searchMovieSelectorsByMultiplex(multiplexId, query));
    }

    //Endpoint para obtener  una unica "info" de peli en la cartelera
    @GetMapping("/movie/multiplex/{multiplexId}/selectors/{movieId}")
    public ResponseEntity<MovieSelectorDTO> getMovieSelectorByMultiplexAndMovie(
            @PathVariable UUID multiplexId,
            @PathVariable Long movieId
    ) {
        return ResponseEntity.ok(movieServices.getMovieSelectorByMultiplexAndMovie(multiplexId, movieId));
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
    public ResponseEntity<StatusResponse> deleteMovie(
            @Valid
            @PathVariable UUID idScreening,
            @RequestParam ScreeningStatus status
    ){
        movieAdminService.changeScreeningStatus(idScreening, status);

        return ResponseEntity.ok(
                new StatusResponse(
                    status,
                    idScreening
        ));
    }


    //Record sencillo para respuestas sencillas y propias del controlador
    public record StatusResponse(
            @NotBlank
            ScreeningStatus screeningStatus,
            @NotBlank
            UUID screeningId
    ) { }

}
