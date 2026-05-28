package CinePacho.demo.movie.controller;

import CinePacho.demo.movie.dto.request.CreateScreeningDTO;
import CinePacho.demo.movie.dto.response.MovieResponseDTO;
import CinePacho.demo.movie.dto.response.MovieSearchResponseDTO;
import CinePacho.demo.movie.dto.response.ScreeningResponseDTO;
import CinePacho.demo.movie.enumeration.ScreeningStatus;
import CinePacho.demo.movie.service.MovieAdminService;
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

    @Autowired
    public MovieController(MovieAdminService movieAdminService) {
        this.movieAdminService = movieAdminService;
    }

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
