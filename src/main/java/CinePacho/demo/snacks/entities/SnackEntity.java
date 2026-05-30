package CinePacho.demo.snacks.entities;

import CinePacho.demo.multiplex.entitites.MultiplexEntity;
import jakarta.persistence.*;
import lombok.*;
 
import java.math.BigDecimal;
import java.util.UUID;
 
@Entity
@Table(name = "snacks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SnackEntity {
 
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;
 
    @Column(name = "nombre", nullable = false, length = 100)
    private String name;
 
    @Column(name = "descripcion", length = 500)
    private String description;
 
    @Column(name = "precio", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "cantidad", nullable = false)
    private int quantity;

    @ManyToOne
    @JoinColumn(name = "multiplex_id", nullable = false)
    private MultiplexEntity multiplex;
}
