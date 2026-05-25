package CinePacho.demo.reviews.entitites;

import CinePacho.demo.auth.entities.customers.BuyerEntity;
import CinePacho.demo.reviews.enumeration.ReviewType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "reviews")
public class ReviewEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // un comprador pude hacer varias reseñas
    @ManyToOne
    @JoinColumn(name = "buyer_id")
    private BuyerEntity buyer;

    private Long movieId;            // null si es SERVICE

    private String comment;

    @Enumerated(EnumType.STRING)
    private ReviewType type;         // MOVIE o SERVICE

    private Integer rating;          // 0 a 5
    private LocalDateTime createdAt;

    private String movieTitle;
}
