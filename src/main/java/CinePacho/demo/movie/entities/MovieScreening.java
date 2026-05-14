package CinePacho.demo.movie.entities;

import CinePacho.demo.movie.enumeration.ScreeningStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

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

    @ManyToOne
    @JoinColumn(name = "movie_id")   // ← crea la FK en esta tabla
    private MovieEntity movie;

//    @ManyToOne
//    @JoinColumn(name = "sala_id")
//    private SalaEntity sala;         // ← cuando se tenga la entidad sala

    private LocalDateTime fechaHora;

    @Enumerated(EnumType.STRING)
    private ScreeningStatus status;
}

