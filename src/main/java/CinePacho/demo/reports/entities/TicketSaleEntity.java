package CinePacho.demo.reports.entities;

import CinePacho.demo.movie.entities.MovieScreening;
import CinePacho.demo.multiplex.entitites.MultiplexEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidad que registra cada venta de tickets (sillas) para reportes mensuales.
 */
@Entity
@Table(name = "ticket_sales")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketSaleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // Multiplex donde se realizó la venta.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "multiplex_id", nullable = false)
    private MultiplexEntity multiplex;

    // Función de película asociada a la venta.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "screening_id", nullable = false)
    private MovieScreening screening;

    // Fecha y hora exacta de la venta.
    @Column(name = "sold_at", nullable = false)
    private LocalDateTime soldAt;

    // Cantidad de tickets vendidos en esta transacción.
    @Column(name = "tickets_quantity", nullable = false)
    private Integer ticketsQuantity;

    // Total monetario de los tickets vendidos (sin snacks).
    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;
}
