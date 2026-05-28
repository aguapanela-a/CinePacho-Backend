package CinePacho.demo.auth.entities.customers;

import CinePacho.demo.auth.entities.user.UserEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "buyers")
@Getter @Setter
public class BuyerEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID buyerId;

    @OneToOne
    @JoinColumn(name = "userId", unique = true)
    private UserEntity user;

    @Column
    private long points;

    @Column(name = "correo", unique = true, nullable = false)
    private String email;

    // Tabla para guardar los IDs de las películas que el comprador ha visto (comprado).
    // Se guarda solo el ID para evitar acoplar el módulo de autenticación con el de películas.
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "buyer_watched_movies",
            joinColumns = @JoinColumn(name = "buyer_id"),
            uniqueConstraints = @UniqueConstraint(columnNames = {"buyer_id", "movie_id"}))
    @Column(name = "movie_id")
    private List<Long> watchedMovieIds = new ArrayList<>();
}
