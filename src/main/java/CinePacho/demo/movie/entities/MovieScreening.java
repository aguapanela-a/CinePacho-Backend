package CinePacho.demo.movie.entities;

import CinePacho.demo.movie.enumeration.ScreeningStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "movie_screenings")
public class MovieScreening {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movie_id", nullable = false)   // ← crea la FK en esta tabla
    private MovieEntity movie;

//    @ManyToOne
//    @JoinColumn(name = "sala_id")
//    private RoomEntity sala;         // ← cuando se tenga la entidad sala

    @Column(name = "screen_date_time", nullable = false)
    private LocalDateTime dateTime;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private ScreeningStatus status;

    private BigDecimal price;
}

