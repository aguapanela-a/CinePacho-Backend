package CinePacho.demo.reviews.controller;

import CinePacho.demo.reviews.dto.CreateReviewDto;
import CinePacho.demo.reviews.dto.ReviewDetailResponseDto;
import CinePacho.demo.reviews.dto.ReviewResponseDto;
import CinePacho.demo.reviews.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class ReviewController {

    private final ReviewService reviewService;

    //lista de reviews de una película
    @GetMapping("/review/movie/{movieId}")
    public ResponseEntity<List<ReviewDetailResponseDto>> getReviewsByMovieId(
            @PathVariable Long movieId
    ) {
        return ResponseEntity.ok(reviewService.getReviewsByMovieId(movieId));
    }

    //lista de reviews hechas por un usuario (solo el mismo usuario puede ver su propia lista de reviews)
    @GetMapping("/{buyerId}/review")
    public ResponseEntity<List<ReviewDetailResponseDto>> getReviewsByUserId(
            @RequestHeader("Authorization") String token,
            @PathVariable UUID buyerId) {
        return ResponseEntity.ok(reviewService.getReviewsByUserId(buyerId, token));
    }


    //crear review de peli
    @PostMapping("/{buyerId}/review/movie")
    public ResponseEntity<ReviewResponseDto> createMovieReview(
            @RequestHeader("Authorization") String token,
            @PathVariable UUID buyerId,
            @RequestBody CreateReviewDto dto
    ) {
        return ResponseEntity.ok(reviewService.createMovieReview(buyerId, dto, token));
    }

    // crear review de servicios
    @PostMapping("/{buyerId}/review/service")
    public ResponseEntity<ReviewResponseDto> createServiceReview(
            @RequestHeader("Authorization") String token,
            @PathVariable UUID buyerId,
            @RequestBody CreateReviewDto dto
    ) {
        return ResponseEntity.ok(reviewService.createServiceReview(buyerId, dto, token));
    }
}
