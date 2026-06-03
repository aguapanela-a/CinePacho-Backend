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

        System.out.println("ReviewService: " + dto.toString());

        ReviewEntity review = generateReview(dto, buyerID, ReviewType.SERVICE);
        return new ReviewResponseDto(
                review.getType(),
                review.getComment(),
                review.getRating()
        );
    }


    public ReviewResponseDto createMovieReview(UUID buyerID, CreateReviewDto dto, String token){

        System.out.println("&&&&&&&&&&&& EJECUIÓN  DE CREATE MOVIE REVIEW &&&&&&&&&&&&");

        validateUserIdentity(buyerID, token, "No puedes crear reviews a nombre de otros usuarios");

        if (!movieManager.existsById(dto.movieId())) {
            throw new CinePachoException("Película no encontrada, por favor asegúrese de seleccionar una película existente");
        }

        if(reviewRepository.existsByMovieIdAndBuyer_BuyerId(dto.movieId(), buyerID)){
            throw new CinePachoException("Usted ya ha ha calificado esta película");
        }

        if(!buyerManager.getBuyerById(buyerID).getWatchedMovieIds().contains(dto.movieId())){
            throw new CinePachoException("Para calificar una película ya debió haber visto la película");
        }

        // crea y guarda la review
        ReviewEntity review = generateReview(dto, buyerID, ReviewType.MOVIE);

        //Tomo el rating actual de la peli
        Double currentRating = movieManager.getRating(dto.movieId());

        System.out.println("/////////////-->> CURRENT RATING: " + currentRating + " <<--/////////////");

        //Sumo el nuevo rating y lo divido entre la cantidad de reviews
        Double newRating = (currentRating + dto.rating())/(reviewRepository.countByMovieId(dto.movieId()));

        System.out.println("/////////////-->> NEW RATING: " + newRating + " <<--/////////////");

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

        //compara user id's de ambos
        if (currentUser.getUserType().name().equals("BUYER") && !currentUser.getUserId().equals(targetUser.getUserId())) {
                throw new CinePachoException(errorMessage);
            }

    }

    private ReviewEntity generateReview(CreateReviewDto dto, UUID buyerID, ReviewType type){
        ReviewEntity review = new ReviewEntity();

        review.setBuyer(buyerManager.getBuyerById(buyerID));
        review.setRating(dto.rating());
        review.setComment(dto.comment());
        review.setCreatedAt(LocalDateTime.now());
        review.setType(type);

        // Solo buscar la película si la reseña es de tipo MOVIE y el ID no es null
        if (type == ReviewType.MOVIE && dto.movieId() != null) {
            review.setMovieId(dto.movieId());
            review.setMovieTitle(movieManager.getMovieTitle(dto.movieId()));
        }

        System.out.println("[@@@@@@@@@@@@@@@@@] buyer id: " + buyerID + "[@@@@@@@@@@@@@@@@@]");


        System.out.println("[@@@@@@@@@@@@@@@@@] movie id: " + dto.movieId() + "[@@@@@@@@@@@@@@@@@]");

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
