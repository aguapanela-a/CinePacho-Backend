package CinePacho.demo.points.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;
// entidad para seleccionar si se suman puntos por unidad o por 'combo de compra'
@Entity
@Table(name = "points_config")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PointsConfigEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // true = by unit, false = by purchase
    @Column(name = "by_unit", nullable = false)
    private boolean byUnit;
}