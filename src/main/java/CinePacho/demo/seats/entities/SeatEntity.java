package CinePacho.demo.seats.entities;

import CinePacho.demo.rooms.entities.RoomEntity;
import CinePacho.demo.seats.enumeration.SeatStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

import CinePacho.demo.shared.enumeration.SeatType;
 
@Entity
@Table(
        name = "seats",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_seat_room_number_type",
                columnNames = {"room_id", "seat_number", "type"}
                //para que la combinación de id de sala, numero de silla y tipo sean unicos
        )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeatEntity {
 
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private RoomEntity room;
 
    @Column(name = "seat_number", nullable = false)
    private Integer seatNumber;
 
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private SeatType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private SeatStatus status;

    @Column(name = "blocked_by_user_id")
    private String blockedByUserEmail;

    @Column(name = "blocked_until")
    private LocalDateTime blockedUntil;
}