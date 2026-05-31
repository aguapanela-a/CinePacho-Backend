package CinePacho.demo.seats.entities;

import CinePacho.demo.movie.entities.MovieScreening;
import CinePacho.demo.seats.enumeration.SeatStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "seat_screenings",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_seat_screening",
                columnNames = {"seat_id", "screening_id"}
        )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeatScreeningEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seat_id", nullable = false)
    private SeatEntity seat;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "screening_id", nullable = false)
    private MovieScreening screening;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private SeatStatus status;

    @Column(name = "blocked_by_user_email")
    private String blockedByUserEmail;

    @Column(name = "blocked_until")
    private LocalDateTime blockedUntil;
}
