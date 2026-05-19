package CinePacho.demo.movie.controller;

import CinePacho.demo.movie.dto.CreateScreeningDTO;
import CinePacho.demo.movie.dto.MovieResponseDTO;
import CinePacho.demo.movie.dto.ScreeningResponseDTO;
import CinePacho.demo.movie.dto.TmdbMovieDTO;
import CinePacho.demo.movie.enumeration.ScreeningStatus;
import CinePacho.demo.movie.service.MovieService;
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
    private final MovieService movieService;

    @Autowired
    public MovieController(MovieService movieService) {
        this.movieService = movieService;
    }

    @GetMapping("/admin/movie/search")
    public ResponseEntity<List<TmdbMovieDTO>> searchMovie(
            @Valid
            @RequestParam String query,
            @RequestParam(defaultValue = "1") int page
    ) {
        return ResponseEntity.ok(movieService.searchMovie(query, page));
    }


    @PostMapping("/admin/movie/select/{movieId}")
    public ResponseEntity<MovieResponseDTO> selectMovie(
            @Valid
            @PathVariable Long movieId
    ) {
        return ResponseEntity.ok(movieService.selectMovie(movieId));
    }


    @PostMapping("/admin/movie/createScreening")
    public ResponseEntity<ScreeningResponseDTO> createScreening(
            @Valid
            @RequestBody CreateScreeningDTO dto
    ) {
        return ResponseEntity.ok(movieService.createScreening(dto));
    }


    @PutMapping("/admin/movie/changeStatus/{idScreening}")
    public ResponseEntity<StatusResponse> deleteMovie(
            @Valid
            @PathVariable UUID idScreening,
            @RequestParam ScreeningStatus status
    ){
        movieService.changeScreeningStatus(idScreening, status);

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
