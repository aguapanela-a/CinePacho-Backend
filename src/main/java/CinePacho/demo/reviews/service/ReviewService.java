package CinePacho.demo.reviews.service;

import CinePacho.demo.auth.entities.user.UserEntity;
import CinePacho.demo.exception.CinePachoException;
import CinePacho.demo.reviews.dto.CreateReviewDto;
import CinePacho.demo.reviews.dto.ReviewDetailResponseDto;
import CinePacho.demo.reviews.dto.ReviewResponseDto;
import CinePacho.demo.reviews.entitites.ReviewEntity;
import CinePacho.demo.reviews.enumeration.ReviewType;
import CinePacho.demo.reviews.repository.ReviewRepository;
import CinePacho.demo.shared.auxiliaryClass.BuyerManager;
import CinePacho.demo.shared.auxiliaryClass.MovieManager;
import CinePacho.demo.shared.auxiliaryClass.UserManager;
import CinePacho.demo.shared.serviceSecurity.JwtService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final BuyerManager buyerManager;
    private final MovieManager movieManager;
    private final JwtService jwtService;
    private final UserManager userManager;

    public ReviewService(ReviewRepository reviewRepository, BuyerManager buyerManager, MovieManager movieManager, JwtService jwtService, UserManager userManager) {
        this.reviewRepository = reviewRepository;
        this.buyerManager = buyerManager;
        this.movieManager = movieManager;
        this.jwtService = jwtService;
        this.userManager = userManager;
    }

    // Lista de reviews de un movie por id de la movie
    public List<ReviewDetailResponseDto> getReviewsByMovieId(Long movieId) {
        return reviewRepository.findAllByMovieId(movieId)
                .stream()
                .map(this::toReviewDetailResponse)
                .toList();
    }


    // lista de reviews de una movie por usuario
    public List<ReviewDetailResponseDto> getReviewsByUserId(UUID userId, String token) {

        validateUserIdentity(userId, token, "No puedes ver las reviews de otros usuarios");

        return reviewRepository.findAllByBuyer_BuyerId(userId)
                .stream()
                .map(this::toReviewDetailResponse)
                .toList();
    }


    public ReviewResponseDto createServiceReview(UUID buyerID, CreateReviewDto dto, String token){
        validateUserIdentity(buyerID, token, "No puedes crear reviews a nombre de otros usuarios");

        ReviewEntity review = generateReview(dto, buyerID, ReviewType.SERVICE);
        return new ReviewResponseDto(
                review.getType(),
                review.getComment(),
                review.getRating()
        );
    }


    public ReviewResponseDto createMovieReview(UUID buyerID, CreateReviewDto dto, String token){

        validateUserIdentity(buyerID, token, "No puedes crear reviews a nombre de otros usuarios");

        if (!movieManager.existsById(dto.movieId())) {
            throw new CinePachoException("Película no encontrada, por favor asegúrese de seleccionar una película existente");
        }

        if(reviewRepository.existsByMovieIdAndBuyer_BuyerId(dto.movieId(), buyerID)){
            throw new CinePachoException("Usted ya ha ha calificado esta película");
        }

        // crea y guarda la review
        ReviewEntity review = generateReview(dto, buyerID, ReviewType.MOVIE);

        //Tomo el rating actual de la peli
        Double currentRating = movieManager.getRating(dto.movieId());

        //Sumo el nuevo rating y lo divido entre la cantidad de reviews
        Double newRating = (currentRating + dto.rating())/(reviewRepository.countByMovieId(dto.movieId()));

        //Guardo el nuevo rating en la BD
        movieManager.updateRating(dto.movieId(), newRating);

        return new ReviewResponseDto(
                review.getType(),
                review.getComment(),
                review.getRating()
        );
    }

    private void validateUserIdentity(UUID targetUserId, String token, String errorMessage) {
        String userCurrEmail = jwtService.extractEmail(token);

        //Extrae el UserEntity del toke actual
        UserEntity currentUser = userManager.getUserByEmail(userCurrEmail);
        //Extrae el UserEntity del targetUserId (que es buyer)
        UserEntity targetUser = buyerManager.getBuyerById(targetUserId).getUser();

        if (currentUser.getUserType().name().equals("BUYER")) {
            if (!currentUser.getUserId().equals(targetUser.getUserId())) {
                //TODO: Borrar ids de prueba xd
                throw new CinePachoException(errorMessage + " user autenticado:" + currentUser.getUserId() + "// target:" + targetUserId);
            }
        }
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
        review.setMovieTitle(movieManager.getMovieTitle(dto.movieId()));

        return reviewRepository.save(review);
    }

    private ReviewDetailResponseDto toReviewDetailResponse(ReviewEntity review) {
        return new ReviewDetailResponseDto(
                review.getId(),
                review.getMovieId(),
                review.getType(),
                review.getComment(),
                review.getRating(),
                review.getCreatedAt(),
                review.getMovieTitle()
        );
    }

}
