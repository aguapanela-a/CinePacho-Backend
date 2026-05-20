package CinePacho.demo.rooms.entities;

import CinePacho.demo.multiplex.entitites.MultiplexEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
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
 
    @JoinColumn(name = "multiplex_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private MultiplexEntity multiplex;
 
    @Column(name = "general_capacity", nullable = false)
    private Integer generalCapacity;
 
    @Column(name = "preferential_capacity", nullable = false)
    private Integer preferentialCapacity;
 
    @Column(name = "active", nullable = false)
    @Builder.Default
    private Boolean active = true;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

}