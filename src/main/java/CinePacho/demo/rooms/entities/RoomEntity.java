package CinePacho.demo.rooms.entities;

import jakarta.persistence.*;
import lombok.*;
 
import java.util.UUID;
 
@Entity
@Table(name = "rooms")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomEntity {
 
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;
 
    // Relación con Multiplex (solo FK por ahora; ajusta si ya tienes la entidad)
    @Column(name = "multiplex_id", nullable = false)
    private UUID multiplexId;
 
    @Column(name = "number_room", nullable = false)
    private Integer numberRoom;
 
    @Column(name = "general_capacity", nullable = false)
    private Integer generalCapacity;
 
    @Column(name = "preferential_capacity", nullable = false)
    private Integer preferentialCapacity;
 
    @Column(name = "active", nullable = false)
    @Builder.Default
    private Boolean active = true;
}