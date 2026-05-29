package CinePacho.demo.movie.controller;

import CinePacho.demo.movie.dto.*;
import CinePacho.demo.movie.enumeration.ScreeningStatus;
import CinePacho.demo.movie.service.MovieScreeningService;
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
    private final MovieScreeningService movieScreeningService;

    @Autowired
    public MovieController(MovieService movieService, MovieScreeningService movieScreeningService) {
        this.movieService = movieService;
        this.movieScreeningService = movieScreeningService;
    }

    @GetMapping("/admin/movie/search")
    public ResponseEntity<List<MovieSearchResponseDTO>> searchMovie(
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


    //Cartelera de peliculas 
    @GetMapping("/movieListing")
    public ResponseEntity<List<MovieListingResponseDTO>> getMovieListing() {
        return ResponseEntity.ok(movieScreeningService.getMovieListing());
    }

    //Top 10 peliculas con más rating
    @GetMapping("/topRatedMovies")
    public ResponseEntity<List<MovieListingResponseDTO>> getTopRatedMovies() {
        return ResponseEntity.ok(movieScreeningService.getTop10Movies());
    }



    //Record sencillo para respuestas sencillas y propias del controlador
    public record StatusResponse(
            @NotBlank
            ScreeningStatus screeningStatus,
            @NotBlank
            UUID screeningId
    ) { }

}
