package CinePacho.demo.reviews.repository;

import CinePacho.demo.reviews.entitites.ReviewEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ReviewRepository extends JpaRepository<ReviewEntity, UUID> {

    List<ReviewEntity> findAllByMovieId(Long movieId);

    List<ReviewEntity> findAllByBuyer_BuyerId(UUID buyerBuyerId);
}
