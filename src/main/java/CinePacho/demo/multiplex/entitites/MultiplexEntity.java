package CinePacho.demo.multiplex.entitites;

import java.math.BigDecimal;
import java.util.UUID;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "multiplex")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MultiplexEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String city;
    
    @Column(nullable = false)
    private String address;

    // Precios por tipo de silla configurables por multiplex (ADMIN/MANAGER)
    @Column(name = "general_seat_price", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal generalSeatPrice = BigDecimal.valueOf(11000);

    // Precio preferencial por multiplex (ADMIN/MANAGER)
    @Column(name = "preferential_seat_price", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal preferentialSeatPrice = BigDecimal.valueOf(15000);
}
