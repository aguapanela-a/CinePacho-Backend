package CinePacho.demo.points.entities;

import CinePacho.demo.auth.entities.customers.BuyerEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "points_gained")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PointsGainedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "buyer_id", nullable = false)
    private BuyerEntity buyer;

    @Column(name = "points", nullable = false)
    private Integer points;

    @Column(name = "description")
    private String description;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

}