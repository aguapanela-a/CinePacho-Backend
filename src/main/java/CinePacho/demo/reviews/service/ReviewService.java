package CinePacho.demo.reviews.service;

import CinePacho.demo.exception.CinePachoException;
import CinePacho.demo.reviews.dto.CreateReviewDto;
import CinePacho.demo.reviews.dto.ReviewResponseDto;
import CinePacho.demo.reviews.entitites.ReviewEntity;
import CinePacho.demo.reviews.enumeration.ReviewType;
import CinePacho.demo.reviews.repository.ReviewRepository;
import CinePacho.demo.shared.auxiliaryClass.BuyerManager;
import CinePacho.demo.shared.auxiliaryClass.MovieManager;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final BuyerManager buyerManager;
    private final MovieManager movieManager;

    public ReviewService(ReviewRepository reviewRepository, BuyerManager buyerManager, MovieManager movieManager) {
        this.reviewRepository = reviewRepository;
        this.buyerManager = buyerManager;
        this.movieManager = movieManager;
    }

    // Lista de reviews de un movie por id de la movie
    public List<ReviewEntity> getReviewsByMovieId(Long movieId) {
        return reviewRepository.findAllByMovieId(movieId);
    }

    // lista de reviews de una movie por usuario
    public List<ReviewEntity> getReviewsByUserId(UUID userId) {
        return reviewRepository.findAllByBuyer_BuyerId(userId);
    }

    public ReviewResponseDto createServiceReview(UUID buyerID, CreateReviewDto dto){
        ReviewEntity review = generateReview(dto, buyerID, ReviewType.SERVICE);
        return new ReviewResponseDto(
                review.getType(),
                review.getComment(),
                review.getRating()
        );
    }


    public ReviewResponseDto createMovieReview(UUID buyerID, CreateReviewDto dto){

        if (!movieManager.existsById(dto.movieId())) {
            throw new CinePachoException("Película no encontrada, por favor asegúrese de seleccionar una película existente");
        }

        if(reviewRepository.existsByMovieIdAndBuyer_BuyerId(dto.movieId(), buyerID)){
            throw new CinePachoException("Usted ya ha ha calificado esta película");
        }

        ReviewEntity review = generateReview(dto, buyerID, ReviewType.MOVIE);

        return new ReviewResponseDto(
                review.getType(),
                review.getComment(),
                review.getRating()
        );
    }

    //TODO: Hacer historial de películas vistas justo después pagar, quizá una nueva entidad que guarde nombre de peli, calificación del usuario y fecha de visualización
    // pero ahi si no sé como serán las relaciones, quizá un pelócula puede estar en muchos históricos, pero un histórico solo puede tener una peli
    // uno a uno con Buyer quizá y ya -> esto para validar que no califique peliculas no vistas

    private ReviewEntity generateReview(CreateReviewDto dto, UUID buyerID, ReviewType type){
        ReviewEntity review = new ReviewEntity();

        review.setBuyer(buyerManager.getBuyerById(buyerID));
        review.setMovieId(dto.movieId());
        review.setRating(dto.rating());
        review.setComment(dto.comment());
        review.setCreatedAt(LocalDateTime.now());
        review.setType(type);

        return reviewRepository.save(review);
    }

}
